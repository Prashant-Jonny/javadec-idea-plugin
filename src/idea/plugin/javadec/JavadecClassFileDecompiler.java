package idea.plugin.javadec;

import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.fileTypes.BinaryFileDecompiler;
import com.intellij.openapi.fileTypes.ContentBasedFileSubstitutor;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.compiled.ClsFileImpl;
import javadec.Decompiler;
import org.jetbrains.annotations.NotNull;

public class JavadecClassFileDecompiler implements BinaryFileDecompiler {


    private final Decompiler decompiler = new Decompiler();

    @NotNull
    @Override
    public CharSequence decompile(VirtualFile file) {
        assert file.getFileType() == StdFileTypes.CLASS;

        final Project[] projects = ProjectManager.getInstance().getOpenProjects();
        if (projects.length == 0) return "";
        final Project project = projects[0];

        final ContentBasedFileSubstitutor[] processors = Extensions.getExtensions(ContentBasedFileSubstitutor.EP_NAME);
        for (ContentBasedFileSubstitutor processor : processors) {
            if (processor.isApplicable(project, file)) {
                return processor.obtainFileText(project, file);
            }
        }

        try {
            byte[] bytes = FileUtil.loadBytes(file.getInputStream());
            // The header MUST have length 119
            // It automatically collapsed by IDE
            String header = "\n                                                     \n" +
                    "  // Decompiled with JavaDec\n" +
                    "  // see http://javadec.github.io\n" +
                    "\n";
            return header + decompiler.decompile(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ClsFileImpl.decompile(PsiManager.getInstance(project), file);
        }

    }
}
