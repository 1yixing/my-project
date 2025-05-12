package org.example;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;

public class A_1_9_3_Check{
    //循环体内的continue需要限制使用
    public void check(ICPPASTTranslationUnit translationUnit, String filePath){
        translationUnit.accept(new ASTVisitor(true) {
            @Override
            public int visit(IASTStatement node) {
                if(node instanceof IASTForStatement){
                    ((IASTForStatement)node).getBody().accept(new ASTVisitor(true) {
                        int count=0;
                        @Override
                        public int visit(IASTStatement statement) {

                            if(statement instanceof IASTContinueStatement){
                                count++;
                                if(count>1)
                                {
                                    System.out.println(1);
                                }
                            }
                            return super.visit(statement);
                        }

                    });
                }
                if(node instanceof IASTWhileStatement){
                    ((IASTWhileStatement)node).getBody().accept(new ASTVisitor(true) {
                        int count=0;
                        @Override
                        public int visit(IASTStatement statement) {
                            if(statement instanceof IASTContinueStatement){
                                count++;
                                if(count>1)
                                {
                                    System.out.println(1);
                                }
                            }
                            return super.visit(statement);
                        }
                    });
                }
                if(node instanceof IASTDoStatement){
                    ((IASTDoStatement)node).getBody().accept(new ASTVisitor(true) {
                        int count=0;
                        @Override
                        public int visit(IASTStatement statement) {
                            if(statement instanceof IASTContinueStatement){
                                count++;
                                if(count>1)
                                {
                                    System.out.println(statement.getFileLocation().getEndingLineNumber());
                                }
                            }
                            return super.visit(statement);
                        }
                    });
                }
                return super.visit(node);
            }
        });
    }
}
