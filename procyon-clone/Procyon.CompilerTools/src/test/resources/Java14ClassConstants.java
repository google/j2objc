public class Java14ClassConstants {
    void f() {
        System.out.println(Integer.class);
        System.out.println(Java14ClassConstants.class);
        
        class Local {
            void f() {
                System.out.println(Integer.class);
                System.out.println(Local.class);
                System.out.println(Java14ClassConstants.class);
            }
        }
        
        new Local() {
            void f() {
                System.out.println(Integer.class);
                System.out.println(Local.class);
                System.out.println(Java14ClassConstants.class);
            }
        };
    }
    
    class Inner {
        void f() {
            System.out.println(Integer.class);
            System.out.println(Java14ClassConstants.Inner.class);
            System.out.println(Java14ClassConstants.class);

            class Local {
                void f() {
                    System.out.println(Integer.class);
                    System.out.println(Local.class);
                    System.out.println(Java14ClassConstants.class);
                }
            }

            new Local() {
                void f() {
                    System.out.println(Integer.class);
                    System.out.println(Local.class);
                    System.out.println(Java14ClassConstants.class);
                }
            };
        }
    }

    static class StaticInner {
        void f() {
            System.out.println(Integer.class);
            System.out.println(Java14ClassConstants.StaticInner.class);
            System.out.println(Java14ClassConstants.class);

            class Local {
                void f() {
                    System.out.println(Integer.class);
                    System.out.println(Local.class);
                    System.out.println(Java14ClassConstants.class);
                }
            }

            new Local() {
                void f() {
                    System.out.println(Integer.class);
                    System.out.println(Local.class);
                    System.out.println(Java14ClassConstants.class);
                }
            };
        }
    }
}
