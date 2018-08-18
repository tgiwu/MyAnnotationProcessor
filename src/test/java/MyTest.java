import com.google.testing.compile.JavaFileObjects;
import com.zhy.processor.InjectProcessor;
import org.junit.Test;
import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

import javax.tools.JavaFileObject;

public class MyTest {
    @Test
    public void testGenerateCode() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.Test",
                ""
                + "package test;\n"
                        + "import android.view.View;\n"
                        + "import com.zhy.annotation.InjectView;\n"
                        + "public class Test {\n"
                        + "    @InjectView(1) View thing;\n"
                        + "}");
        JavaFileObject bindResource = JavaFileObjects.forSourceString("test/Test_PROXY",
                        "package test; \n" +
                                "\n" +
                                "import android.view.View;\n" +
                                "import com.zhy.processor.Finder;\n" +
                                "import com.zhy.processor.AbstractInjector;\n" +
                                "\n" +
                                "public class Test_PROXY<T extends Test> implements AbstractInjector<T> { \n" +
                                "    @Override \n" +
                                "    public void inject(final Finder finder, final T target, Object source) {\n" +
                                "        View view;\n" +
                                "        view = finder.findViewById( source, 1);\n" +
                                "        target.thing = finder.castView(view, 1, \"thing\" );    }\n" +
                                "\n" +
                                "}");

            assertAbout(javaSource()).that(source)
                    .withCompilerOptions("-Xlint:-processing")
                    .processedWith(new InjectProcessor())
                    .compilesWithoutWarnings()
                    .and()
                    .generatesSources(bindResource);

    }
}
