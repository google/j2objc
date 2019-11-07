public class Java12ClassConstants {
    void f() {
        System.out.println(Integer.class);
        System.out.println(Java12ClassConstants.class);
        
        class Local {
            void f() {
                System.out.println(Integer.class);
                System.out.println(Local.class);
                System.out.println(Java12ClassConstants.class);
            }
        }
        
        new Local() {
            void f() {
                System.out.println(Integer.class);
                System.out.println(Local.class);
                System.out.println(Java12ClassConstants.class);
            }
        };
    }
    
    class Inner {
        void f() {
            System.out.println(Integer.class);
            System.out.println(Java12ClassConstants.Inner.class);
            System.out.println(Java12ClassConstants.class);

            class Local {
                void f() {
                    System.out.println(Integer.class);
                    System.out.println(Local.class);
                    System.out.println(Java12ClassConstants.class);
                }
            }

            new Local() {
                void f() {
                    System.out.println(Integer.class);
                    System.out.println(Local.class);
                    System.out.println(Java12ClassConstants.class);
                }
            };
        }
    }

    static class StaticInner {
        void f() {
            System.out.println(Integer.class);
            System.out.println(Java12ClassConstants.StaticInner.class);
            System.out.println(Java12ClassConstants.class);

            class Local {
                void f() {
                    System.out.println(Integer.class);
                    System.out.println(Local.class);
                    System.out.println(Java12ClassConstants.class);
                }
            }

            new Local() {
                void f() {
                    System.out.println(Integer.class);
                    System.out.println(Local.class);
                    System.out.println(Java12ClassConstants.class);
                }
            };
        }
    }
}
