package ru.vyarus.dropwizard.guice.web

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import org.eclipse.jetty.ee10.servlet.ServletHolder
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

import jakarta.inject.Inject
import jakarta.servlet.annotation.WebServlet
import jakarta.servlet.http.HttpServlet

/**
 * @author Vyacheslav Rusakov
 * @since 22.08.2016
 */
@TestGuiceyApp(GCollApp)
class ServletGenNameCollisionTest extends Specification {

    @Inject
    Environment environment

    def "Check servlet name generation"() {

        expect: "OServlet name without postfix cut"
        ServletHolder oservlet = environment.getApplicationContext().getServletHandler().getServlet(".oservlet")
        oservlet.registration.className == OServlet.name

        and: "Servlet name without postfix cut"
        ServletHolder servlet = environment.getApplicationContext().getServletHandler().getServlet(".servlet")
        servlet.registration.className == Servlet.name
    }

    static class GCollApp extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(OServlet, Servlet)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }

    @WebServlet("/foo")
    static class OServlet extends HttpServlet {}

    @WebServlet("/bar")
    static class Servlet extends HttpServlet {}
}