package ispd.application.terminal;

import static ispd.application.terminal.HasMessageIn.*;
import static org.approvaltests.Approvals.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import ispd.annotations.*;
import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.regex.*;
import org.apache.commons.cli.*;
import org.hamcrest.core.*;
import org.jetbrains.annotations.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

class TerminalApplicationCharacterizationTest {

    public static final Path MODEL_FOLDER_PATH = Path.of("src", "test", "resources", "models");

    private static final CharSequence FILE_NAME_DELIMITER = "_";

    private static final String[] NO_OPTIONS = {};

    private static final Pattern SPACE_MATCHER = Pattern.compile(" ");

    private final PrintStream standardOut = System.out;

    private final ByteArrayOutputStream outStream = new ByteArrayOutputStream();

    @RefactorOnNextJavaLts(reason = "Use Pattern Matching.")
    private static String @Nullable [] convertToOptionArray (final CharSequence spacedOptions) {
        if (spacedOptions == null) {
            return null;
        }
        if (spacedOptions.isEmpty()) {
            return NO_OPTIONS;
        }
        return SPACE_MATCHER.split(spacedOptions);
    }

    private static TerminalApplication initTerminalApplication (final CharSequence spacedOptions) {
        return new TerminalApplication(convertToOptionArray(spacedOptions));
    }

    private static void runTerminalApplication (final CharSequence spacedOptions) {
        initTerminalApplication(spacedOptions).run();
    }

    private static void runApplicationOnModelWith (
        final String users,
        final String icons,
        final String load
    ) {
        final String modelName = String.join(FILE_NAME_DELIMITER, users, icons, load);
        runTerminalApplication(ModelFolder.GRID.pathToModel(modelName));
    }

    private String systemOutContents () {
        return this.outStream.toString();
    }

    private <T> CombinableMatcher<Throwable> hasMessageInSysOut_andIsOfType (final Class<T> type) {
        return both(hasMessageIn(this.systemOutContents())).and(is(instanceOf(type)));
    }

    @BeforeEach
    void replaceSystemOut () {
        System.setOut(new PrintStream(this.outStream, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void reinstateStandardSystemOut () {
        System.setOut(this.standardOut);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void givenEmptyOrNullOptions_thenThrowsOnInit (final String options) {
        final var exception = assertThrows(
            IllegalArgumentException.class,
            () -> initTerminalApplication(options)
        );

        assertThat(exception, hasMessageIn(this.systemOutContents()));

        verify(this.systemOutContents());
    }

    @Test
    void givenUnrecognizedOption_thenThrowsOnInit () {
        final var cause = assertThrows(
            RuntimeException.class,
            () -> initTerminalApplication("-z")
        ).getCause();

        assertThat(cause, this.hasMessageInSysOut_andIsOfType(UnrecognizedOptionException.class));

        verify(this.systemOutContents());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "-P",
            "--port",
            "-e",
            "-t",
            "-a",
        }
    )
    void givenOptionWithMissingArgument_thenThrowsOnInit (final String options) {
        final var cause = assertThrows(
            RuntimeException.class,
            () -> initTerminalApplication(options)
        ).getCause();

        assertThat(cause, this.hasMessageInSysOut_andIsOfType(MissingArgumentException.class));
    }

    @Test
    void givenOptionWithMissingArgument_thenPrintsErrorOnInit () {
        try {
            initTerminalApplication("-P");
        } catch (final RuntimeException ignored) {
            // ... throwing behavior already tested
        }

        verify(this.systemOutContents());
    }

    @Test
    void givenInvalidAddress_thenThrowsOnInit () {
        final var cause = assertThrows(
            IllegalArgumentException.class,
            () -> initTerminalApplication("-a NotAnAddress")
        ).getCause();

        assertThat(cause, this.hasMessageInSysOut_andIsOfType(UnknownHostException.class));

        verify(this.systemOutContents());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "-P NaN",
            "-e NaN",
            "-t NaN",
        }
    )
    void givenOptionWithInvalidNumberArgument_thenThrowsOnInit (final String options) {
        final var cause = assertThrows(
            RuntimeException.class,
            () -> initTerminalApplication(options)
        ).getCause();

        assertThat(cause, is(instanceOf(NumberFormatException.class)));

        verify(this.systemOutContents());
    }

    @Test
    void givenValidOptions_thenPrintsNothingOnInit () {
        initTerminalApplication("-h");

        assertThat(
            "Should not print anything to out on valid initialization.",
            this.systemOutContents(),
            is(emptyString())
        );
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "-h",
            "--help",
            "-h -v",
            "-v -h",
            "-h nonexistent.imsx",
            "nonexistent.imsx -h",
        }
    )
    void givenHelpOption_thenPrintsHelpWhenRun (final String options) {
        runTerminalApplication(options);

        verify(this.outStream);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "-v",
            "--version",
            "-v nonexistent.imsx",
            "nonexistent.imsx -v",
        }
    )
    void givenVersionOption_thenPrintsVersionInfoWhenRun (final String options) {
        runTerminalApplication(options);

        verify(this.outStream);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "nonexistent.imsx",
            "wrongExtension",
            "wrongExtension.txt",
        }
    )
    void givenNonexistentOrWrongExtensionModel_thenPrintsErrorWhenRun (final String modelName) {
        runTerminalApplication(ModelFolder.NO_TYPE.pathToFile(modelName));

        assertThat(
            "Error message printed to out should contain model name",
            this.systemOutContents(),
            containsString(modelName)
        );

        verify(
            this.outStream.toString().replace(modelName, "")
        );
    }

    @Test
    void givenEmptyModelFile_thenPrintsErrorWhileOpeningModel () {
        runTerminalApplication(ModelFolder.NO_TYPE.pathToModel("empty"));

        verify(this.outStream);
    }

    @Test
    void givenModelFileWithInvalidXml_thenPrintsErrorWhileOpeningModel () {
        runTerminalApplication(ModelFolder.GRID.pathToModel("malformed"));

        verify(this.outStream);
    }

    @Test
    void givenModelWithNoUsers_thenPrintsErrorAfterOpeningModel () {
        runApplicationOnModelWith("noUsers", "noIcons", "noLoad");

        verify(this.outStream);
    }

    @Test
    void givenModelWithNoIcons_thenPrintsErrorAfterOpeningModel () {
        runApplicationOnModelWith("oneUser", "noIcons", "noLoad");

        verify(this.outStream);
    }

    @Test
    void givenModelWithNoLoad_thenPrintsErrorAfterOpeningModel () {
        runApplicationOnModelWith("oneUser", "oneMachineIcon", "noLoad");

        verify(this.outStream);
    }

    @Test
    void givenModelWithNoMasters_thenPrintsErrorAfterOpeningModel () {
        runApplicationOnModelWith("oneUser", "oneMachineIcon", "oneTaskGlobalLoad");

        verify(this.outStream);
    }

    @Test
    void givenModelWithInvalidSchedulingPolicy_thenThrowsWhileInterpretingModel () {
        final var cause = assertThrowsExactly(
            RuntimeException.class,
            () -> runApplicationOnModelWith(
                "oneUser", "oneMachineMasterIcon", "oneTaskGlobalLoad"
            )
        ).getCause();

        assertThat(
            cause,
            both(is(instanceOf(ClassNotFoundException.class)))
                .and(hasProperty("message", containsString("---")))
        );

        verify(this.outStream);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "oneMachineSchedulerIcon",
        }
    )
    void givenModelWithMistake_thenThrowsAfterCreatingTasks (final String icons) {
        assertThrows(
            RuntimeException.class,
            () -> runApplicationOnModelWith(
                "oneUser", icons, "oneTaskGlobalLoad"
            )
        );

        verify(this.outStream);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "schedulerDefaultLinkSlaveIcons",
            "schedulerDefaultBiLinkSlaveIcons",
        }
    )
    void givenModelWithImproperLinks_thenThrowsAfterCreatingTasks (final String icons) {
        assertThrows(
            LinkageError.class,
            () -> runApplicationOnModelWith(
                "oneUser", icons, "oneTaskGlobalLoad"
            )
        );

        verify(this.outStream);
    }
}
