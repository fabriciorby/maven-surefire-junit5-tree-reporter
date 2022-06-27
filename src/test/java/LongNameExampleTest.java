import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Long Name Sample Test")
public class LongNameExampleTest {

    static final String[] sonnet133 = {
            "For that deep wound it gives my friend and me!",
            "Is’t not enough to torture me alone,",
            "But slave to slavery my sweet’st friend must be?",
            "Me from myself thy cruel eye hath taken,",
            "And my next self thou harder hast engrossed:",
            "Of him, myself, and thee I am forsaken;",
            "A torment thrice three-fold thus to be crossed.",
            "Prison my heart in thy steel bosom’s ward,",
            "But then my friend’s heart let my poor heart bail;",
            "Whoe’er keeps me, let my heart be his guard;",
            "Thou canst not then use rigour in my jail:",
            "And yet thou wilt; for I, being pent in thee,",
            "Perforce am thine, and all that is in me."
    };

    static final String[] sonnet138 = {
            "When my love swears that she is made of truth\n",
            "I do believe her, though I know she lies,\n",
            "That she might think me some untutor’d youth,\n",
            "Unlearned in the world’s false subtleties.\n",
            "Thus vainly thinking that she thinks me young,\n",
            "Although she knows my days are past the best,\n",
            "Simply I credit her false speaking tongue:\n",
            "On both sides thus is simple truth suppress’d.\n",
            "But wherefore says she not she is unjust?\n",
            "And wherefore say not I that I am old?\n",
            "O, love’s best habit is in seeming trust,\n",
            "And age in love loves not to have years told:\n",
            "Therefore I lie with her and she with me,\n",
            "And in our faults by lies we flatter’d be.\n"
    };

    static final String[] sonnet144 = {
            "Two loves I have of comfort and despair,\r\n",
            "Which like two spirits do suggest me still:\r\n",
            "The better angel is a man right fair,\r\n",
            "The worser spirit a woman coloured ill.\r\n",
            "To win me soon to hell, my female evil,\r\n",
            "Tempteth my better angel from my side,\r\n",
            "And would corrupt my saint to be a devil,\r\n",
            "Wooing his purity with her foul pride.\r\n",
            "And whether that my angel be turned fiend,\r\n",
            "Suspect I may, yet not directly tell;\r\n",
            "But being both from me, both to each friend,\r\n",
            "I guess one angel in another’s hell:\r\n",
            "Yet this shall I ne’er know, but live in doubt,\r\n",
            "Till my bad angel fire my good one out.\r\n"
    };

    static Stream<Arguments> shakespeare() {
        return Stream.of(
                Arguments.of((Object) sonnet133),
                Arguments.of((Object) sonnet138),
                Arguments.of((Object) sonnet144)
        );
    }

    @ParameterizedTest
    @MethodSource
    @DisplayName("Test normalization and abbreviation of test report")
    void shakespeare(String[] sonnets) {
        assertTrue(true);
    }
}
