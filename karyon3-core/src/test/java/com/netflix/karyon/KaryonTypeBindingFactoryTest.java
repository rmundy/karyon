package com.netflix.karyon;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class KaryonTypeBindingFactoryTest {
    public static interface Baz {
        String getName();
    }
    
    public static interface Bar {
        String getName();
    }
    
    public static class Foo {
        private Bar bar;
        private Baz baz;
        private Bar namedBar;

        @Inject
        public Foo(Bar bar, Baz baz, @Named("bar") Bar namedBar) {
            this.bar = bar;
            this.baz = baz;
            this.namedBar = namedBar;
        }
    }
    
    @Test
    public void test() {
        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                this.bind(Foo.class).asEagerSingleton();
                this.bindListener(TypeLiteralMatchers.subclassOf(Bar.class), new TypeListener() {
                    @Override
                    public <I> void hear(TypeLiteral<I> type,
                            TypeEncounter<I> encounter) {
                        System.out.println(type);
                    }
                });
            }
        });
    }
    
    @Test(expected=CreationException.class)
    public void confirmCreateFailsWithMissingBinding() {
        Karyon.create()
            .addModules(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(Foo.class).asEagerSingleton();
                }
            })
            .start();
    }
    
    @Test
    public void testAddBinding() {
        Injector injector = Karyon.create()
            .addModules(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(Foo.class).asEagerSingleton();
                }
            })
            .addAutoBinder(
                TypeLiteralMatchers.subclassOf(Bar.class),
                new AutoBinder() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public <T> boolean configure(Binder binder, final Key<T> key) {
                        binder.bind(key).toInstance((T)new Bar() {
                            @Override
                            public String getName() {
                                return null == key.getAnnotation() ? "" : key.getAnnotation().toString();
                            } 
                        });
                        return true;
                    }
                }
            )
            .addAutoBinder(
                TypeLiteralMatchers.subclassOf(Baz.class),
                new AutoBinder() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public <T> boolean configure(Binder binder, Key<T> key) {
                        binder.bind(key).toInstance((T)new Baz() {
                            @Override
                            public String getName() {
                                return null == key.getAnnotation() ? "" : key.getAnnotation().toString();
                            }
                        });
                        return true;
                    }
                }
            )
            .start();
            
        Foo foo = injector.getInstance(Foo.class);
        
        assertThat(foo.bar.getName(),       equalTo(""));
        assertThat(foo.baz.getName(),       equalTo(""));
        assertThat(foo.namedBar.getName(),  equalTo("@com.google.inject.name.Named(value=bar)"));
    }
}   
