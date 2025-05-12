package org.example;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

import java.util.HashSet;
import java.util.Set;

public class R_1_10_2_Check{
    //规则：长整型变量赋值给短整型必须强制转换
    /*
    思路：
    收集所有二元表达式存入集合
    过滤非赋值表达式
    检查左右表达式是否为整型比如char或者int
    判断是否存在强制转换
    如果有长整型赋值给短整型且不存在强制转换则报错
    */
    Set<IASTBinaryExpression> binaryExpressions=new HashSet<>();
    public void check(ICPPASTTranslationUnit icppastTranslationUnit, String arg){
        icppastTranslationUnit.accept(new ASTVisitor(true) {
            //在 AST 中，IASTBinaryExpress和IASTSimpleDelaration 不共享相同父子关系，需要通过不同路径解析：
           @Override
           public int visit(IASTExpression node) {
               //存储所有的二元表达式
               if(node instanceof IASTBinaryExpression&&
                       ((IASTBinaryExpression) node).getOperand2()!=null)
               //虽然说二元表达式的第二个参数一般存在，但仍有可能会有式子不完全的情况，因此判断以保证程序的健壮性
               {
                   //存储所有的二元表达式
                   binaryExpressions.add((IASTBinaryExpression) node);
               }
               return super.visit(node);
           }
           @Override
            public int visit(IASTDeclaration declaration) {
               if(declaration instanceof IASTSimpleDeclaration)
               {
                   IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) declaration;
                   IType var=null;
                   for(IASTDeclarator declarator:simpleDeclaration.getDeclarators())
                   {
                       if(declarator.getInitializer()!=null&&
                       declarator.getInitializer() instanceof IASTEqualsInitializer&&
                               ((IASTEqualsInitializer) declarator
                                       .getInitializer())
                                       .getInitializerClause() instanceof IASTExpression)
                       {
                           if(var==null)var= CPPVisitor.createType(declarator);
                           IASTExpression expression=(IASTExpression) ((IASTEqualsInitializer) declarator
                                   .getInitializer())
                                   .getInitializerClause();
                           //IASTSimpleDeclaration是定义，而对应的equal操作就是赋值，所以要排除常数赋值
                           if(expression instanceof CPPASTLiteralExpression)continue;
                           IType var2=expression.getExpressionType();
                           //var是头部的定义，而var2是表达式的最高数据类型，现在需要比较两个数据
                           if(checkRule(var,var2))
                           {
                               System.out.println("第"+declaration.getFileLocation().getStartingLineNumber()+"行有误");
                           }
                       }
                   }
               }
               return super.visit(declaration);
           }
        });
        binaryExpressions
                .stream()
                .filter(x->x.getOperator()>=IASTBinaryExpression.op_assign
                        &&x.getOperator()<=IASTBinaryExpression.op_minusAssign)
                /*从op_assign到op_minusAssign对应的有 =，*=，/=，%=，+=，-=，都是赋值操作，
                有些是复合赋值操作也需要判断右边的表达式是否符合要求*/
                .filter(x->{
                    if(x.getOperand2()==null)
                    {
                        return false;
                    }
                    IASTExpression leftExp=x.getOperand1();
                    IASTExpression rightExp=x.getOperand2();
                    if(rightExp instanceof CPPASTLiteralExpression)
                    {
                        return false;
                    }
                    IType rightType=rightExp.getExpressionType();
                    IType leftType=leftExp.getExpressionType();
                    return checkRule(leftType,rightType);
                })
                .forEach(this::print);
    }

    private void print(IASTBinaryExpression binaryExpression) {
        System.out.println("第"+binaryExpression.getFileLocation().getStartingLineNumber()+"行有误");
    }
    public Boolean checkRule(IType var,IType var2)
    {
        //第一步要判断两个数据类型是否为整型
        boolean p1=isIntegralType(var),p2=isIntegralType(var2);
        if(!p1||!p2)
        {
            //非整型赋值转换，不予考虑
            return false;
        }
        IBasicType v1=(IBasicType) var,v2=(IBasicType) var2;
        return comparavar(v1,v2);
    }
    public IType getUnderlyingType(IType type) {
        IType current = type;
        while (true) {
            if (current instanceof IQualifierType) { // const/volatile
                current = ((IQualifierType) current).getType();
            } else if (current instanceof ITypedef) { // typedef 别名
                current = ((ITypedef) current).getType();
            } else {
                return current;
            }
        }
    }
    public  boolean isIntegralType(IType type) {
        IType baseType = getUnderlyingType(type);

        if (baseType instanceof IBasicType) {
            IBasicType basicType = (IBasicType) baseType;
            switch (basicType.getKind()) {
                case eChar:
                case eInt:
                case eInt128:
                    return true;

            }
        }
        return false;
    }
    public Boolean comparavar(IBasicType v1,IBasicType v2)
    {
        int sum1=0,sum2=0;
        sum1+=v1.isShort()?-1:0;
        sum2+=v2.isShort()?-1:0;
        sum1+=v1.isLong()?1:0;
        sum2+=v2.isLong()?1:0;
        sum1+=v1.isLongLong()?2:0;
        sum2+=v2.isLongLong()?2:0;
        return sum1<sum2;
    }
}