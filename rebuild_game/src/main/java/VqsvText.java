final class VqsvText {
    private VqsvText() {
    }

    static final class Common {
        private Common() {
        }

        static final String MINIMAP_TASK_HELP = "Nh\u1ea5n n\u00fat 0 tra x\u00e9t ti\u1ec3u \u0111\u1ecba \u0111\u1ed3, nh\u1ea5n n\u00fat 1 tra x\u00e9t nhi\u1ec7m v\u1ee5.";
        static final String UNKNOWN_SPEAKER = "??";
        static final String SOURCE_PET_REWARD_FALLBACK = "S\u1ee7ng v\u1eadt";
        static final String MONEY_REWARD_PREFIX = "\u0110\u1ea1t \u0111\u01b0\u1ee3c: ";
        static final String MONEY_REWARD_SUFFIX = " kim ti\u1ec1n";
        static final String BADGE_REWARD_SUFFIX = " Huy hi\u1ec7u";
        static final String MONEY_LOST_PREFIX = "M\u1ea5t: ";
        static final String BADGE_LOST_SUFFIX = " huy hi\u1ec7u";
        static final String ITEM_REWARD_PREFIX = "\u0110\u1ea1t \u0111\u01b0\u1ee3c: ";
        static final String ITEM_LOST_PREFIX = "M\u1ea5t: ";
        static final String ITEM_BAG_FULL = "Ba l\u00f4 \u0111\u00e3 \u0111\u1ee7 \u0111\u1ea1o c\u1ee5 n\u00e0y";
        static final String PROMPT_PRESS_0 = "Nh\u1ea5n n\u00fat 0 \u0111\u1ec3 ti\u1ebfp t\u1ee5c";
        static final String SMOKE_SANDWICH_X10 = "B\u00e1nh Sandwich x 10";
    }

    static final class Battle {
        private Battle() {
        }

        static final String CAPTURE_LABEL = "B\u1eaft";
        static final String[][] COMMAND_LABELS = {
                {"Chi\u1ebfn", "\u0111\u1ea5u"},
                {"B\u1eaft", "\u0111\u01b0\u1ee3c"},
                {"\u0110\u1ea1o", "c\u1ee5"},
                {"S\u1ee7ng", "v\u1eadt"},
                {"Th\u01b0\u01a1ng", "\u0111i\u1ebfm"},
                {"Ch\u1ea1y", "tr\u1ed1n"}
        };
        static final String[] COMMAND_PROMPTS = {
                "Chi\u1ebfn \u0111\u1ea5u",
                "B\u1eaft \u0111\u01b0\u1ee3c",
                "\u0110\u1ea1o c\u1ee5",
                "S\u1ee7ng v\u1eadt",
                "Th\u01b0\u01a1ng \u0111i\u1ebfm",
                "Ch\u1ea1y tr\u1ed1n"
        };
        static final String START = "B\u1eaft \u0111\u1ea7u tr\u1eadn";
        static final String COMMAND_FIGHT = "Chi\u1ebfn \u0111\u1ea5u";
        static final String COMMAND_CATCH_PENDING = "B\u1eaft \u0111\u01b0\u1ee3c";
        static final String COMMAND_ITEM_PENDING = "\u0110\u1ea1o c\u1ee5";
        static final String COMMAND_PET_PENDING = "S\u1ee7ng v\u1eadt";
        static final String COMMAND_SHOP_PENDING = "Th\u01b0\u01a1ng \u0111i\u1ebfm";
        static final String COMMAND_RUN_PENDING = "Ch\u1ea1y tr\u1ed1n";
        static final String WARNING_PROMPT = "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c";
        static final String SKILL_TITLE = "K\u1ef9 n\u0103ng";
        static final String SKILL_PP_TITLE = "S\u1ed1 l\u1ea7n";
        static final String SKILL_USE = "S\u1eed d\u1ee5ng";
        static final String SKILL_NO_PP = "K\u1ef9 n\u0103ng gi\u00e1 tr\u1ecb ch\u01b0a \u0111\u1ee7";
        static final String SKILL_EMPTY = "Kh\u00f4ng c\u00f3 k\u1ef9 n\u0103ng gi\u00e1 tr\u1ecb, kh\u00f4ng c\u00e1ch n\u00e0o chi\u1ebfn \u0111\u1ea5u";
        static final String CATCH_NOT_ALLOWED = "Tr\u1eadn chi\u1ebfn n\u00e0y kh\u00f4ng cho b\u1eaft s\u1ee7ng v\u1eadt";
        static final String NO_BALLS = "S\u1ed1 l\u01b0\u1ee3ng Pokemon ball kh\u00f4ng \u0111\u1ee7";
        static final String CATCH_SUCCESS = "B\u1eaft th\u00e0nh c\u00f4ng #2";
        static final String CATCH_FAILED = "\u0110\u00e1ng ti\u1ebfc \u0111\u00e3 b\u1eaft tr\u01b0\u1ee3t";
        static final String CATCH_RELEASED_FULL = "Kh\u00f4ng c\u00f2n kh\u00f4ng gian, s\u1ee7ng v\u1eadt n\u00e0y \u0111\u00e3 ph\u00f3ng sinh";
        static final String CATCH_SENT_BANK = "S\u1ee7ng v\u1eadt ba l\u00f4 \u0111\u00e3 \u0111\u1ee7, \u0111\u00e3 \u0111\u1ec3 v\u00e0o ng\u00e2n h\u00e0ng";
        static final String CATCH_STORAGE_FULL = "Ng\u00e2n h\u00e0ng v\u00e0 Ba l\u00f4 \u0111\u1ec1u \u0111\u00e3 \u0111\u1ea7y";
        static final String ITEM_BIND_WARNING = "Tr\u1ea1ng th\u00e1i b\u1ecb qu\u1ea5n, kh\u00f4ng th\u1ec3 s\u1eed d\u1ee5ng \u0111\u1ea1o c\u1ee5";
        static final String PET_BIND_WARNING = "Tr\u1ea1ng th\u00e1i b\u1ecb qu\u1ea5n, kh\u00f4ng th\u1ec3 \u0111\u1ed5i s\u1ee7ng v\u1eadt";
        static final String RUN_BIND_WARNING = "Tr\u1ea1ng th\u00e1i b\u1ecb qu\u1ea5n, kh\u00f4ng th\u1ec3 ch\u1ea1y tr\u1ed1n";
        static final String RUN_NOT_ALLOWED = "Tr\u1eadn chi\u1ebfn n\u00e0y kh\u00f4ng th\u1ec3 tr\u1ed1n ch\u1ea1y";
        static final String RUN_FAILED = "Ch\u1ea1y tr\u1ed1n th\u1ea5t b\u1ea1i";
        static final String RUN_SUCCESS = "Ch\u1ea1y tr\u1ed1n";
        static final String NO_ITEMS = "Kh\u00f4ng c\u00f3 \u0111\u1ea1o c\u1ee5";
        static final String ITEM_NOT_IN_BATTLE = "Trong chi\u1ebfn \u0111\u1ea5u kh\u00f4ng th\u1ec3 s\u1eed d\u1ee5ng";
        static final String NO_ITEM_COUNT = "\u0110\u00e3 kh\u00f4ng c\u00f3 \u0111\u1ea1o n\u00e0y c\u1ee5, th\u1ec9nh mua s\u1eafm";
        static final String NO_PET_TARGET = "S\u1ee7ng v\u1eadt n\u00e0y kh\u00f4ng c\u00f3, kh\u00f4ng th\u1ec3 s\u1eed d\u1ee5ng";
        static final String ITEM_USED = "Th\u00e0nh c\u00f4ng s\u1eed d\u1ee5ng \u0111\u1ea1o c\u1ee5";
        static final String NO_SWITCH_PET = "Kh\u00f4ng c\u00f3 s\u1ee7ng v\u1eadt c\u00f3 th\u1ec3 thay \u0111\u1ed5i";
        static final String PET_SWITCHED = "\u0110\u00e3 thay \u0111\u1ed5i s\u1ee7ng v\u1eadt: ";
        static final String NO_SHOP_ITEMS = "Kh\u00f4ng c\u00f3 \u0111\u1ea1o c\u1ee5 b\u00e1n";
        static final String NOT_ENOUGH_MONEY = "Kim ti\u1ec1n kh\u00f4ng \u0111\u1ee7";
        static final String DAMAGE = " g\u00e2y ";
        static final String DAMAGE_SUFFIX = " s\u00e1t th\u01b0\u01a1ng";
        static final String NEIL_LOST = "Neil th\u1ea5t th\u1ee7, result ";
        static final String BUNNY_WEAK = "Bunny b\u1ecb th\u01b0\u01a1ng, d\u00f9ng phong \u1ea5n c\u1ea7u";
        static final String BALL_CHOSEN = "\u0110\u00e3 ch\u1ecdn phong \u1ea5n c\u1ea7u";
        static final String BUNNY_CAUGHT = "B\u1eaft \u0111\u01b0\u1ee3c Bunny, result ";
        static final String ELDER_DONE = "Tr\u1eadn s\u00e1t h\u1ea1ch ho\u00e0n t\u1ea5t";
    }

    static final class Scene0Intro {
        private Scene0Intro() {
        }

        static final String[] TEXT = {
                "#FFFFFF Nghe \u0111\u1ed3n Thi\u00ean \u0110\u1ecba chi s\u01a1, v\u1ea1n n\u0103m v\u1ec1 tr\u01b0\u1edbc c\u00f3 hai v\u1ecb th\u1ea7n, m\u1ed9t ng\u01b0\u1eddi duy tr\u00ec tr\u1eadt t\u1ef1, m\u1ed9t ng\u01b0\u1eddi cai qu\u1ea3n th\u1ebf gi\u1edbi h\u1ed7n lo\u1ea1n, ki\u1ec1m ch\u1ebf l\u1eabn nhau, duy tr\u00ec c\u00e2n b\u1eb1ng c\u1ee7a th\u1ebf gi\u1edbi.",
                "#FFFFFF Vi B\u1ea1ch Long, v\u1ecb th\u1ea7n \u0111\u1ee9ng \u0111\u1ea7u Thi\u00ean Gi\u1edbi ph\u1ee5 tr\u00e1ch cai qu\u1ea3n tr\u1eadt t\u1ef1. Ba v\u1ecb th\u1ee7 h\u1ed9 th\u00e1nh th\u00fa l\u1ea7n l\u01b0\u1ee3t l\u00e0 L\u00f4i K\u1ef3 L\u00e2n, Tinh V\u00e2n H\u1ea1c c\u00f9ng Minh V\u01b0\u01a1ng Long.",
                "#FFFFFF Vi H\u1eafc Long, v\u1ecb th\u1ea7n \u0111\u1ee9ng \u0111\u1ea7u \u0110\u1ecba Gi\u1edbi ph\u1ee5 tr\u00e1ch cai qu\u1ea3n th\u1ebf gi\u1edbi h\u1ed7n lo\u1ea1n. B\u1ed1n v\u1ecb chi\u1ebfn th\u1ea7n th\u00fa l\u1ea7n l\u01b0\u1ee3t l\u00e0 Chi\u1ebfn Th\u1ea7n \u0110\u00e0, T\u01b0\u01a1ng Qu\u00e2n Gi\u1ea3i, Linh Quang L\u1ed9c v\u00e0 H\u1ecfa Ph\u01b0\u1ee3ng Ho\u00e0ng.",
                "#FFFFFF M\u1ea5y ng\u00e0n n\u0103m tr\u01b0\u1edbc, l\u1ef1c l\u01b0\u1ee3ng h\u1ed7n \u0111\u1ed9n th\u1ebf l\u1ef1c kh\u00f4ng ng\u1eebng l\u1edbn m\u1ea1nh, d\u1ea7n h\u00ecnh th\u00e0nh xu th\u1ebf \u0111\u00e0n \u00e1p Thi\u00ean Gi\u1edbi. \u0110\u1ec3 c\u00e2n b\u1eb1ng gi\u1eefa Thi\u00ean \u0110\u1ecba, B\u1ea1ch Long c\u00f9ng H\u1eafc Long \u0111\u00e3 ti\u1ebfn h\u00e0nh m\u1ed9t cu\u1ed9c Thi\u00ean \u0110\u1ecba th\u00e1nh chi\u1ebfn.",
                "#FFFFFF M\u1ea5y tr\u0103m n\u0103m sau, cu\u1ed9c chi\u1ebfn k\u1ebft th\u00fac, B\u1ea1ch Long c\u00f9ng H\u1eafc Long \u0111\u1ec1u tan bi\u1ebfn.",
                "#FFFFFF Kh\u00f4ng l\u00e2u sau \u0111\u00f3, th\u1ebf gian xu\u1ea5t hi\u1ec7n hai B\u1ea3o Ch\u00e2u, m\u1ed9t tr\u1eafng, m\u1ed9t \u0111en. Ng\u01b0\u1eddi ta tin r\u1eb1ng \u0111\u00e2y ch\u00ednh l\u00e0 linh h\u1ed3n c\u1ee7a c\u00e1c v\u1ecb th\u1ea7n c\u1ed5 \u0111\u1ea1i, c\u00f3 n\u0103ng l\u01b0\u1ee3ng v\u00f4 t\u1eadn.",
                "#FFFFFF V\u1ec1 sau, hai B\u1ea3o Ch\u00e2u n\u00e0y, m\u1ed9t bay l\u00ean Thi\u00ean Gi\u1edbi, m\u1ed9t r\u01a1i xu\u1ed1ng nh\u00e2n gian, ti\u1ebfp t\u1ee5c s\u1ee9 m\u1ec7nh b\u1ea3o v\u1ec7 th\u1ebf gi\u1edbi.",
                "#FFFFFF Thi\u00ean Gi\u1edbi v\u00e0 \u0110\u1ecba Gi\u1edbi c\u00f3 m\u1ed1i li\u00ean h\u1ec7 duy nh\u1ea5t th\u00f4ng \u0111\u1ea1o Thi\u00ean Gi\u1edbi B\u1ea1ch Long Th\u1ea7n \u0110i\u1ec7n c\u00f9ng \u0110\u1ecba Gi\u1edbi H\u1eafc Long Th\u1ea7n \u0110i\u1ec7n. C\u1ee9 sau m\u1ed9t tr\u0103m n\u0103m, hai t\u00f2a th\u1ea7n \u0111i\u1ec7n m\u1edf l\u1ed1i \u0111i th\u00f4ng nhau v\u00e0o m\u1ed9t ng\u00e0y \u0111\u1ec3 ng\u01b0\u1eddi hai gi\u1edbi c\u00f3 th\u1ec3 g\u1eb7p g\u1ee1. Nh\u01b0ng m\u1ed9t tr\u0103m n\u0103m m\u1edbi c\u00f3 m\u1ed9t c\u01a1 h\u1ed9i n\u00ean c\u00f3 th\u1ec3 n\u00f3i \u0111\u00e2y c\u0169ng kh\u00f4ng h\u1eb3n \u0111\u00e3 l\u00e0 ni\u1ec1m vui cho nh\u00e2n lo\u1ea1i.",
                "#FFFFFFH\u1eafc Th\u1ea1ch Th\u00e0nh M\u00e3 \u0110\u1ea7u: Ha ha! Tuy l\u00e0 tr\u0103m n\u0103m m\u1edbi c\u00f3 m\u1ed9t d\u1ecbp nh\u01b0ng \u0111\u00e2y c\u0169ng l\u00e0 c\u01a1 h\u1ed9i t\u1ed1t. \u00dd tr\u1eddi \u0111\u00e3 \u0111\u1ecbnh! Ch\u00fang qu\u00e2n nghe l\u1ec7nh!",
                "#FFFFFF H\u1eafc Long Qu\u00e2n:!",
                "#FFFFFF Ng\u00e0y n\u00e0o \u0111\u00f3, T\u1ea5t c\u1ea3 thi\u00ean kh\u00f4ng th\u1ea7n \u0111i\u1ec7n c\u0169ng kh\u00f4ng th\u1ec3 tho\u00e1t kh\u1ecfi ki\u1ebfp \u0111\u1ecbnh n\u00e0y. \u0110\u00e2y kh\u00f4ng ph\u1ea3i chi\u1ebfn tranh, cu\u1ed9c chi\u1ebfn c\u1ee7a m\u1ed9t phe, c\u0103n b\u1ea3n ch\u00ednh l\u00e0... Ch\u1ebft ch\u00f3c.",
                "#FFFFFF M\u1ed9t ng\u00e0y sau \u0111\u00f3, tr\u01b0\u1edbc m\u1ed9t ng\u00f4i \u0111\u1ec1n hoang ..."
        };
    }

    static final class Scene1Room3BeforeTenYears {
        private Scene1Room3BeforeTenYears() {
        }

        static final String[] TEXT = {
                "#FFFFFF S\u00e1u n\u0103m sau ...",
                "\u0110\u1ebfn \u0111\u00e2y \u0111i! Sophie ~ T\u00ecm kh\u00f4ng th\u1ea5y ta \u0111\u00e2u~~~ Ha ha",
                "... H\u00ea h\u00ea ... \u00f4ng tr\u1ed1n sau \u0111\u00e1 Peepna c\u1ee7a t\u00f4i nh\u00ecn l\u00e9n ch\u1ee9 g\u00ec? ~ Mau ra \u0111\u00e2y ~",
                "\u1eb6c, sao ph\u00e1t hi\u1ec7n gi\u1ecfi v\u1eady ta?...",
                "Hun? Th\u1eadt \u0111\u1ea5y ~",
                "\u00c1ch ... s\u1edbm bi\u1ebft kh\u00f4ng ph\u1ea3i.",
                "H\u00ec h\u00ec ~ th\u1eddi gian kh\u00f4ng c\u00f2n s\u1edbm, ch\u00fang ta mau tr\u1edf v\u1ec1 ~",
                "Sophie, \u0111\u00e3 qua v\u00e0i n\u0103m t\u00f4i mu\u1ed1n g\u1eb7p cha m\u1eb9 c\u1eadu.",
                "Ai? V\u00ec sao ch\u1ee9?",
                "\u0110\u01b0\u01a1ng nhi\u00ean l\u00e0 b\u1edfi v\u00ec ch\u01b0a t\u1eebng g\u1eb7p h\u1ecd!",
                "Th\u1ef1c ra..., ch\u00ednh ta c\u0169ng ch\u01b0a t\u1eebng \u0111\u01b0\u1ee3c g\u1eb7p h\u1ecd. M\u1ecdi ng\u01b0\u1eddi \u0111\u1ec1u n\u00f3i cha m\u1eb9 ta \u0111\u00e3 m\u1ea5t trong chi\u1ebfn tranh. T\u1ea5t c\u1ea3 nh\u1eefng g\u00ec c\u00f2n l\u1ea1i c\u1ee7a h\u1ecd ch\u1ec9 c\u00f3 chi\u1ebfc v\u00f2ng c\u1ed5 n\u00e0y.",
                "Neil, tr\u00f4ng b\u1ed9 d\u1ea1ng c\u00f3 v\u1ebb t\u00e2m tr\u1ea1ng th\u1ebf h\u1ea3?",
                "\u1edc th\u00ec ng\u01b0\u1eddi ta \u0111\u1ed3ng c\u1ea3m v\u1edbi c\u1ea3nh ng\u1ed9 c\u1ee7a c\u1eadu! \u0110\u00e1ng th\u01b0\u01a1ng qu\u00e1. Hix",
                "Ta kh\u00f4ng c\u1ea3m th\u1ea5y v\u1eady. M\u1eb7c d\u00f9 ta c\u0169ng mu\u1ed1n c\u00f3 cha m\u1eb9, nh\u01b0ng ta c\u00f3 gia gia, c\u00f3 Neil l\u00e0m b\u1ea1n th\u1ebf l\u00e0 \u0111\u00e3 qu\u00e1 \u0111\u1ee7 r\u1ed3i! Nh\u1ea5t l\u00e0 \u0111\u01b0\u1ee3c s\u1ed1ng m\u1ed9t n\u01a1i v\u1edbi Neil l\u00e0 ni\u1ec1m vui l\u1edbn nh\u1ea5t c\u1ee7a ta!",
                "V\u1eady ch\u00fang ta s\u1ebd c\u00f9ng nhau \u0111i \u0111\u1ebfn b\u1ea5t c\u1ee9 \u0111\u00e2u.",
                "Th\u1eadt s\u1ef1 sao? Neil c\u00f9ng v\u1edbi Sophie sao?",
                "N\u00f3i l\u1ea1i \u0111i ~ Neil th\u1eadt s\u1ef1 s\u1ebd c\u00f9ng v\u1edbi Sophie sao?",
                "\u0110\u01b0\u01a1ng nhi\u00ean! Nam nh\u00e2n \u0111\u1ea1i tr\u01b0\u1ee3ng phu n\u00f3i m\u1ed9t l\u1eddi kh\u00f4ng thay \u0111\u1ed5i! Ta s\u1ebd \u1edf b\u00ean, b\u1ea3o v\u1ec7, kh\u00f4ng cho b\u1ea5t c\u1ee9 ai l\u00e0m t\u1ed5n th\u01b0\u01a1ng Sophie!",
                " Hay qu\u00e1, ta \u01b0\u1edbc \u0111\u01b0\u1ee3c c\u00f9ng Neil s\u1ed1ng chung m\u1ed9t n\u01a1i, v\u0129nh vi\u1ec5n kh\u00f4ng xa r\u1eddi nhau.",
                "\u1eea, nh\u1ea5t \u0111\u1ecbnh.",
                "T\u00ecm \u0111\u01b0\u1ee3c r\u1ed3i! R\u1ed1t cu\u1ed9c \u0111\u00e3 t\u00ecm \u0111\u01b0\u1ee3c! Ng\u01b0\u1eddi mang d\u1ea5u \u1ea5n m\u00e0u h\u1ed3ng!",
                "C\u00e1c ng\u01b0\u01a1i mu\u1ed1n l\u00e0m g\u00ec!?",
                "A a a! Th\u1ea3 ta ra! Neil!",
                "H\u1ed7n x\u01b0\u1ee3c! Bu\u00f4ng Sophie ra!",
                "\u00c1i ch\u00e0, xem ra ti\u1ec3u t\u1eed n\u00e0y mu\u1ed1n l\u00e0m anh h\u00f9ng c\u1ee9u m\u1ef9 nh\u00e2n \u0111\u00e2y.",
                "Gi\u1ea3i quy\u1ebft nhanh t\u00ean n\u00e0y tr\u1edf v\u1ec1 ph\u1ee5c m\u1ec7nh.",
                "\u1ea2i \u1ea3i, kh\u00f4ng ph\u1ea3i ta khi d\u1ec5 ng\u01b0\u01a1i, l\u00e0 ng\u01b0\u01a1i kh\u00f4ng bi\u1ebft t\u1ef1 l\u01b0\u1ee3ng s\u1ee9c m\u00ecnh mu\u1ed1n c\u00f9ng ta \u0111\u1ea5u m\u1ed9t chuy\u1ebfn.",
                "Neil! C\u1eadu l\u00e0m sao...?!",
                "Y\u00ean t\u00e2m, Ta c\u00f2n c\u00f3 th\u1ec3...",
                "Neil! Neil! Ch\u1ea1y mau \u0111i!",
                "\u0110i th\u00f4i, kh\u00f4ng c\u00f3 th\u1eddi gian \u0111\u00f9a v\u1edbi t\u00ean ti\u1ec3u t\u1eed \u0111\u00f3.",
                "#FFFFFF T\u1ee9c th\u1eadt! Sophie! Tr\u1ea3 Sophie l\u1ea1i cho ta ...!(v\u1eeba m\u1edbi th\u1ec1 s\u1ebd b\u1ea3o v\u1ec7 n\u00e0ng. V\u1eeba m\u1edbi h\u1ee9a h\u1eb9n \u0111i \u0111\u00e2u c\u0169ng c\u00f3 nhau, v\u0129nh vi\u1ec5n \u0111em l\u1ea1i ni\u1ec1m vui cho Sophie. Th\u1ebf m\u00e0...)",
                "#FFFFFF \u0110\u00e1m x\u1ea5c x\u01b0\u1ee3c n\u00e0y! H\u00e3y khoan!",
                "\u0110\u00f3 l\u00e0 ... c\u00e1i g\u00ec ...?",
                "Sophie, v\u00f2ng c\u1ed5 ...",
                "Kh\u00f4ng, l\u00e0 ta kh\u00f4ng \u0111\u1ee7 m\u1ea1nh... m\u1ed9t ng\u00e0y n\u00e0o \u0111\u00f3 ... m\u1ed9t ng\u00e0y n\u00e0o \u0111\u00f3!!!"
        };
    }

    static final class Scene1Room0Group0 {
        private Scene1Room0Group0() {
        }

        static final String TEN_YEARS_TITLE = "#FFFFFFM\u01b0\u1eddi n\u0103m sau...";
        static final String NOISE = "#1c6c91Ti\u1ebfng huy\u00ean n\u00e1o...";
        static final String ALI = "Ali";
        static final String TITAN = "Ti-Tan";
        static final String ELDER = "Tr\u01b0\u1edfng th\u00f4n";
        static final String NEIL = "Neil";
        static final String ALI_TALENT = "Neil, th\u00f4n ta nhi\u1ec1u n\u0103m qua c\u00f3 \u00edt thi\u1ebfu ni\u00ean t\u00e0i n\u0103ng, c\u00f2n tr\u1ebb m\u00e0 c\u00f3 th\u1ec3 tham gia cu\u1ed9c thi cu\u1ed9c chi\u1ebfn s\u1ee7ng v\u1eadt Ho\u00e0ng Gia.";
        static final String TITAN_REPLY = "Neil \u0110\u00f3 kh\u00f4ng ph\u1ea3i l\u00e0 thi\u00ean t\u00e0i, khi y \u1edf c\u1eeda hu\u1ea5n luy\u1ec7n s\u1ee7ng v\u1eadt ch\u00e1u s\u1ebd ch\u00ecm v\u00e0o m\u1ed9t gi\u1ea5c ng\u1ee7 ng\u1eafn.";
        static final String ALI_MOTIVE = "C\u00f3 l\u1ebd \u0111\u1ed9ng l\u1ef1c t\u1eeb khi H\u1eafc Long Qu\u00e2n b\u1eaft c\u00f3c ng\u01b0\u1eddi b\u1ea1n thanh mai tr\u00fac m\u00e3 c\u1ee7a y...";
        static final String ELDER_HO = "Ho!";
        static final String ELDER_EXAM = "H\u00f4m nay l\u00e0 th\u1eddi \u0111i\u1ec3m s\u00e1t h\u1ea1ch xem Neil c\u00f3 th\u1ec3 \u0111\u1ee9ng trong \u0111\u1ed9i ng\u0169 chi\u1ebfn \u0111\u1ed9i s\u1ee7ng v\u1eadt Ho\u00e0ng Gia hay kh\u00f4ng, Neil \u0111\u00e3 s\u1eb5n s\u00e0ng ch\u01b0a?";
        static final String NEIL_READY = "Tr\u01b0\u1edfng th\u00f4n, ng\u00e0i bi\u1ebft ta l\u00fac n\u00e0o c\u0169ng s\u1eb5n s\u00e0ng r\u1ed3i \u0111\u1ea5y!";
        static final String ELDER_BUNNY_TASK = "T\u1ed1t l\u1eafm. Neil, ti\u1ebfn v\u1ec1 h\u01b0\u1edbng ph\u00eda \u0111\u00f4ng l\u00e0ng. N\u1ebfu c\u00f3 th\u1ec3 b\u1eaft \u0111\u01b0\u1ee3c th\u1ecf Bunny th\u00ec cu\u1ed9c s\u00e1t h\u1ea1ch coi nh\u01b0 ho\u00e0n th\u00e0nh.";
        static final String NEIL_SIMPLE = "R\u1ea5t \u0111\u01a1n gi\u1ea3n, ch\u1edd m\u1ed9t l\u00e1t";
        static final String TASK_BUNNY = "Ti\u1ebfp nh\u1eadn nhi\u1ec7m v\u1ee5: \u0110\u1ebfn ph\u00eda \u0110\u00f4ng c\u1ee7a l\u00e0ng b\u1eaft s\u1ee7ng v\u1eadt.";
        static final String EAST_BUNNY_HINT = "Gi\u1edd \u0111\u1ebfn g\u1eb7p v\u00e0 ho\u00e0n th\u00e0nh c\u00e1c b\u00e0i ki\u1ec3m tra c\u1ee7a tr\u01b0\u1edfng th\u00f4n! \u0110i ph\u00eda \u0111\u00f4ng v\u1ebd th\u1ecf Bunny!";
    }

    static final class Scene1Room0Group2 {
        private Scene1Room0Group2() {
        }

        static final String ELDER = Scene1Room0Group0.ELDER;
        static final String NEIL = Scene1Room0Group0.NEIL;
        static final String CAUGHT = "B\u1ecb b\u1eaft";
        static final String ELDER_BUNNY_CUTE = "Nh\u1eefng con th\u1ecf tr\u00f4ng d\u1ec5 th\u01b0\u01a1ng l\u00e0m sao.";
        static final String NEIL_WRONG_TARGET = "Tr\u01b0\u1edfng th\u00f4n ... c\u00f3 v\u1ebb m\u1ee5c ti\u00eau sai ...";
        static final String ELDER_PET_OFFER = "Ti\u1ec3u t\u1eed th\u00fai, ng\u01b0\u01a1i bao nhi\u00eau tu\u1ed5i m\u00e0 l\u00ean m\u1eb7t d\u1ea1y ta h\u1ea3? \u0110\u00e2y, Cho ng\u01b0\u01a1i chu\u1ea9n b\u1ecb 3 s\u1ee7ng v\u1eadt. Hi\u1ec7n t\u1ea1i H\u1eafc Long Qu\u00e2n \u0111\u00e3 chi\u1ebfm l\u0129nh ph\u00e2n n\u1eeda \u0111\u1ea1i l\u1ee5c. Chi\u1ebfn \u0111\u1ed9i s\u1ee7ng v\u1eadt Ho\u00e0ng Gia c\u1ee7a ch\u00fang ta th\u00ec \u0111ang gi\u01b0\u01a1ng m\u1eaft \u1ebfch nh\u00ecn. Ng\u01b0\u01a1i mu\u1ed1n \u0111\u1ed1i ph\u00f3 b\u1ecdn h\u1ecd nh\u1ea5t \u0111\u1ecbnh ph\u1ea3i ch\u00fa \u00fd.";
        static final String NEIL_GO_SEE = "T\u1ed1t qu\u00e1! Ta \u0111i xem!";
        static final String ELDER_ONLY_ONE = "\u1eb6c, ta ch\u01b0a n\u00f3i xong. Gi\u1edd ng\u01b0\u01a1i ch\u1ec9 \u0111\u01b0\u1ee3c ch\u1ecdn m\u1ed9t, d\u00f9ng n\u00f3 \u0111\u00e1nh th\u1eafng ta m\u1edbi c\u00f3 th\u1ec3 mang \u0111i.";
        static final String NEIL_NOT_FREE = "Th\u1ebf n\u00e0y ch\u1ea3 b\u1eb1ng cho \u00e0?";
        static final String TASK_PET_CHOICE = "L\u1ef1a ch\u1ecdn s\u1ee7ng v\u1eadt c\u00f9ng tr\u01b0\u1edfng th\u00f4n t\u1ef7 th\u00ed.";
    }

    static final class Scene1Room0Group3 {
        private Scene1Room0Group3() {
        }

        static final String ELDER = Scene1Room0Group0.ELDER;
        static final String YES_NO = "C\u00f3, Kh\u00f4ng";
        static final String[] YES_NO_OPTIONS = {"C\u00f3", "Kh\u00f4ng"};
        static final String PENGUIN = "Th\u1ee7y th\u1ee7 c\u00e1nh c\u1ee5t PenGuin: S\u1ee7ng v\u1eadt thu\u1ed9c h\u1ec7 Th\u1ee7y, c\u00f3 t\u00ednh ch\u1ea5t c\u1ee7a v\u1eadt ch\u1ea5t ch\u1ee9a d\u1ea7u v\u0169 mao, s\u1eed h\u1eafn c\u00f3 th\u1ec3 di chuy\u1ec3n linh ho\u1ea1t.";
        static final String FROG = "\u00d4 L\u00e1 \u1ebech Frog: S\u1ee7ng v\u1eadt thu\u1ed9c h\u1ec7 M\u1ed9c, ph\u1ea7n \u0111u\u00f4i l\u1edbn gi\u00f3ng nh\u01b0 chi\u1ebfc l\u00e1 c\u00f3 th\u1ec3 xem l\u00e0 th\u1ee9 v\u0169 kh\u00ed h\u00e0nh \u0111\u1ed9ng v\u00e0 ph\u00f2ng ng\u1ef1.";
        static final String DRAGON = "R\u1ed3ng B\u1ea3o B\u1ed1i: S\u1ee7ng v\u1eadt thu\u1ed9c h\u1ec7 H\u1ecfa, c\u00f3 kh\u1ea3 n\u0103ng phun ra H\u1ecfa Di\u1ec5m th\u1ea7n k\u1ef3, v\u1ebb b\u1ec1 ngo\u00e0i r\u1ea5t \u0111\u00e1ng y\u00eau nh\u01b0ng \u1ea9n ch\u1ee9a th\u1ef1c l\u1ef1c kh\u00f4ng t\u1ea7m th\u01b0\u1eddng.";
    }

    static final class Items {
        private Items() {
        }

        static final String TAT_TRUNG_CAU = "T\u1ea5t Trung C\u1ea7u";
        static final String PHONG_AN_CAU = "Phong \u1ea5n c\u1ea7u";
        static final String BANH_SANDWICH = "B\u00e1nh Sandwich";
        static final String SINH_MENH_THACH = "Sinh m\u1ec7nh th\u1ea1ch";
        static final String PET_BOOK_PAGE = "Trang s\u00e1ch t\u01b0\u01a1ng \u1ee9ng c\u1ee7a s\u1ee7ng v\u1eadt";
    }

    static final class Scene1Room1Group0 {
        private Scene1Room1Group0() {
        }

        static final String NEIL = Scene1Room0Group0.NEIL;
        static final String BUNNY_REPORT = "Ch\u00ednh l\u00e0 con th\u1ecf c\u1ee7a ng\u01b0\u01a1i, mau gi\u00fap ta b\u00e1o c\u00e1o k\u1ebft qu\u1ea3 \u0111\u1ec3 v\u01b0\u1ee3t qua";
        static final String TASK_RETURN_ELDER = "Tr\u1edf v\u1ec1 t\u00ecm tr\u01b0\u1edfng th\u00f4n!";
    }

    static final class Scene1Room0Group6 {
        private Scene1Room0Group6() {
        }

        static final String ELDER = Scene1Room0Group0.ELDER;
        static final String NEIL = Scene1Room0Group0.NEIL;
        static final String ELDER_ATTACK = "Ti\u1ec3u t\u1eed th\u00fai, ti\u1ebfp chi\u00eau!";
        static final String ELDER_REWARD = "R\u1ea5t t\u1ed1t, nh\u01b0 v\u1eady, ch\u00fang ta c\u0169ng y\u00ean t\u00e2m. Neil, nh\u1eefng v\u1eadt n\u00e0y ng\u01b0\u01a1i mang theo, nh\u1eefng l\u00fac nguy k\u1ecbch s\u1ebd c\u1ea7n d\u00f9ng \u0111\u1ebfn.";
        static final String ELDER_BOOK = "M\u1ed7i khi ng\u01b0\u01a1i nh\u00ecn th\u1ea5y ho\u1eb7c \u0111\u1ea1t \u0111\u01b0\u1ee3c m\u1ed9t s\u1ee7ng v\u1eadt m\u1edbi, s\u00e1ch tranh l\u00fd s\u1ebd gia t\u0103ng ch\u1ee7ng lo\u1ea1i s\u1ee7ng v\u1eadt, do \u0111\u00f3 c\u00e0ng thu th\u1eadp nhi\u1ec1u c\u00e0ng t\u1ed1t.";
        static final String ELDER_ABRA = "Sau khi \u0111\u1ebfn B\u00edch Th\u1ee7y Th\u00e0nh, nh\u1edb t\u00ecm Abra, \u00f4ng \u1ea5y s\u1ebd gi\u00fap ng\u01b0\u01a1i tr\u1edf th\u00e0nh tay hu\u1ea5n luy\u1ec7n s\u1ee7ng v\u1eadt m\u1ea1nh m\u1ebd h\u01a1n.";
        static final String NEIL_REMEMBER = "\u1eecm, ta nh\u1edb r\u1ed3i!";
        static final String TASK_BICH_THUY = "\u0110\u1ebfn B\u00edch Th\u1ee7y Th\u00e0nh.";
        static final String FREE_WORLD = "Gi\u1edd c\u00f3 th\u1ec3 t\u1ef1 do di chuy\u1ec3n.";
    }
}
