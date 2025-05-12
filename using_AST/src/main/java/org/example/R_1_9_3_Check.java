package org.example;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;

import java.util.HashSet;
import java.util.Set;

public class R_1_9_3_Check{
    //禁止在循环内修改循环控制变量
    //前置知识：
    /*
    for循环标准语法：
    for(初始化;条件表达式;迭代语句)
    {
        循环体
    }
    对应了IASTForStatment结构：
    组成部分        | AST结构                          | 获取方法                  | 可能的示例
    初始化表达式      IASTStatement                     getInitializerStatement()   int i=0 or i=5
    循环条件         IASTExpression                    getConditionExpression()    i<10
    迭代表达式        IASTExpression                   getIterationExpression()    i++
    循环体           IASTStatement                     getBody()                  {.....}(复合语句，或者新循环)
    */
    //思路，accept遍历IASTStatment,然后判断是否为循环体，如果为循环体，并且之前没有处理过，则放置递归函数中判断
    //递归函数一方面要处理现存的表达式是否存在规则冲突，另一方面要判断循环体中是否还存在循环结构
    //存在C++的auto类型在循环体内删除东西，更新指针后报错的问题，可能要列一张函数表，表示删除操作的再赋值
    Set<IASTForStatement> st=new HashSet<>();
    Set<String>varnames=new HashSet<>();
    public void check(ICPPASTTranslationUnit translationUnit, String filePath){
        translationUnit.accept(new ASTVisitor(true) {
           @Override
           public int visit(IASTStatement node) {
               if(node instanceof IASTForStatement)
               {
                   IASTForStatement forStatement = (IASTForStatement) node;
                   if(!st.contains(forStatement))
                   {
                       ProcessForStatement(forStatement);
                   }
               }
               return super.visit(node);
           }
        });
    }
    public void ProcessForStatement(IASTForStatement node){

        IASTStatement p=node.getInitializerStatement();
        IASTExpression p2=node.getIterationExpression();
        if(p!=null)
        {
            if(p instanceof IASTDeclarationStatement)
            {
                IASTDeclarationStatement d=(IASTDeclarationStatement) p;
                IASTDeclaration declaration=d.getDeclaration();
                if(declaration instanceof IASTSimpleDeclaration)
                {
                    IASTSimpleDeclaration sd=(IASTSimpleDeclaration) declaration;
                    for(IASTDeclarator declarator:sd.getDeclarators())
                    {
                        if(declarator.getInitializer()!=null&&
                                declarator.getInitializer() instanceof IASTEqualsInitializer)
                        {
                            String[] expression=declarator
                                    .getRawSignature().split("=");
//                            if(varnames.contains(expression[0]))
//                            {
//                                System.out.println("第"+declarator.getFileLocation().getStartingLineNumber()+"行有问题");
//                            }
//                            else
                            varnames.add(expression[0]);
                        }
                    }
                }
            }
            else if(p instanceof IASTExpressionStatement)
            {
                IASTExpression e= ((IASTExpressionStatement) p).getExpression();
                if(e instanceof IASTBinaryExpression)
                {
                    IASTBinaryExpression b=(IASTBinaryExpression) e;
                    if(varnames.contains(b.getOperand1().toString()))
                    {
                        System.out.println("第"+p.getFileLocation().getStartingLineNumber()+"行有问题");
                    }
                    else
                        varnames.add(b.getOperand1().toString());
                }
            }
        }
        if(node.getBody() == null)return;//对于循环体内没有语句的不必考虑了
        node.getBody().accept(new ASTVisitor(true) {
            {
                shouldVisitStatements = true;
                shouldVisitExpressions = true;
            }
            @Override
            public int visit(IASTExpression node)
            {//对于表达式只要判断赋值操作是否会改变循环变量的当前值
                if(node instanceof IASTBinaryExpression) {
                    IASTBinaryExpression binary = (IASTBinaryExpression) node;
                    if(binary.getOperator()>=IASTBinaryExpression.op_assign
                            &&binary.getOperator()<=IASTBinaryExpression.op_minusAssign
                            &&varnames.contains(binary.getOperand1().toString()))
                    {
                        System.out.println("第"+binary.getFileLocation().getStartingLineNumber()+"行出错了");
                    }
                }
                return super.visit(node);
            }
            public int visit(IASTStatement node) {
                if(node instanceof IASTForStatement)
                {
                    IASTForStatement forStatement = (IASTForStatement) node;
                    st.add(forStatement);
                    ProcessForStatement(forStatement);
                    return PROCESS_SKIP;
                }
                return super.visit(node);
            }
        });
        if(p!=null)
        {
            if(p instanceof IASTDeclarationStatement)
            {
                IASTDeclarationStatement d=(IASTDeclarationStatement) p;
                IASTDeclaration declaration=d.getDeclaration();
                if(declaration instanceof IASTSimpleDeclaration)
                {
                    IASTSimpleDeclaration sd=(IASTSimpleDeclaration) declaration;
                    for(IASTDeclarator declarator:sd.getDeclarators())
                    {
                        if(declarator.getInitializer()!=null&&
                                declarator.getInitializer() instanceof IASTEqualsInitializer)
                        {
                            String[] expression=declarator
                                    .getRawSignature().split("=");
                            varnames.remove(expression[0]);
                        }
                    }
                }
            }
            else if(p instanceof IASTExpressionStatement)
            {
                IASTExpression e= ((IASTExpressionStatement) p).getExpression();
                if(e instanceof IASTBinaryExpression)
                {
                    IASTBinaryExpression b=(IASTBinaryExpression) e;
                    varnames.remove(b.getOperand1().toString());
                }
            }
        }
    }
}
