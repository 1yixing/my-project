package org.example;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.parser.*;
import org.eclipse.cdt.internal.core.dom.parser.cpp.*;
import org.eclipse.core.runtime.CoreException;
import java.util.*;
public class Main {
    public static void main(String[] args) throws CoreException {
        IParserLogService log=new DefaultLogService();
        String arg="C:\\Users\\顶宝\\using_AST\\src\\test\\hello.cpp";
        FileContent fileContent=FileContent.createForExternalFileLocation(arg);
        Map<String,String>definedSymbols=new HashMap<>();
        String[] includePaths=new String[0];
        IScannerInfo info= new IScannerInfo() {
            @Override
            public Map<String, String> getDefinedSymbols() {
                return definedSymbols;
            }

            @Override
            public String[] getIncludePaths() {
                return includePaths;
            }
        };
        IncludeFileContentProvider empt=IncludeFileContentProvider.getEmptyFilesProvider();
        int opts=8;
        IASTTranslationUnit translationUnit=GPPLanguage.getDefault().getASTTranslationUnit(
                fileContent,
                info,
                empt,
                null,
                opts,
                log
        );
        R_1_1_7_Check parser=new R_1_1_7_Check();
        if(translationUnit==null) {
            System.out.println("translationUnit==null");
        }
        else if(translationUnit instanceof ICPPASTTranslationUnit) {
            System.out.println("ICPPASTTranslationUnit");
        }
        ICPPASTTranslationUnit p=(ICPPASTTranslationUnit) translationUnit;
        if(p==null) {
            System.out.println("p==null");
        }
        else
            parser.check(p,arg);
        String p1="x>=0?x:-x",p2="x";
        System.out.println(p1.split(p2).length);
        for(String k:p1.split(p2))System.out.println(k);
    }
}
class R_1_1_7_Check{
    public void check(ICPPASTTranslationUnit icppastTranslationUnit, String arg){
        for(IASTPreprocessorMacroDefinition macro:icppastTranslationUnit.getMacroDefinitions()){
            if(macro instanceof IASTPreprocessorFunctionStyleMacroDefinition){
                IASTPreprocessorFunctionStyleMacroDefinition funcMacro =(IASTPreprocessorFunctionStyleMacroDefinition) macro;
                IASTTranslationUnit params = funcMacro.getTranslationUnit();
            }
        }
    }
}