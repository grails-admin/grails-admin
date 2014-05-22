package net.kaleidos.plugins.admin

import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin

import spock.lang.Specification
import spock.lang.Unroll
import spock.lang.Shared

import admin.test.TestDomain
import admin.test.TestDomainRelation

import net.kaleidos.plugins.admin.widget.GrailsAdminPluginWidgetService

import spock.util.mop.ConfineMetaClassChanges

import net.kaleidos.plugins.admin.config.AdminConfigHolder
import net.kaleidos.plugins.admin.config.DomainConfig
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass

import org.codehaus.groovy.grails.commons.DefaultGrailsApplication

@TestFor(GrailsAdminPluginGenericService)
@TestMixin(DomainClassUnitTestMixin)
@ConfineMetaClassChanges([GrailsAdminPluginGenericService])
@Mock([TestDomain, TestDomainRelation])
class GrailsAdminPluginGenericServiceSpec extends Specification {
    @Shared
    def adminConfigHolder

    def setupSpec() {
        def grailsApplication = new DefaultGrailsApplication()
        grailsApplication.configureLoadedClasses([
            admin.test.TestDomain.class,
        ] as Class[])

        def config = new ConfigObject()
        config.grails.plugin.admin.domains = [ "admin.test.TestDomain" ]

        adminConfigHolder = new AdminConfigHolder(config)
        adminConfigHolder.grailsApplication = grailsApplication
        adminConfigHolder.initialize()
    }

    def setup() {
        service.adminConfigHolder = adminConfigHolder
    }

    void "List objects of a domain without filters"() {
        setup:
            mockDomain(TestDomain, [
                [name: movie1, year: year1],
                [name: movie2, year: year2],
                [name: movie3, year: year3] ])
        when:
            def result = service.listDomain(TestDomain)

        then:
            result != null
            result.size() == 3
            result.find { it.name == movie1 } != null
            result.find { it.name == movie2 } != null
            result.find { it.name == movie3 } != null

        where:
            movie1 = "Lord of the Rings"
            year1 = 2001

            movie2 = "Star Wars"
            year2 = 1977

            movie3 = "Matrix"
            year3 = 1999
    }

    @Unroll
    void "Return object"() {
        setup:
            mockDomain(TestDomain, [
                [id: 1, name: movie1, year: year1],
                [id: 2, name: movie2, year: year2],
                [id: 3, name: movie3, year: year3] ])
        when:
            def result = service.retrieveDomain(TestDomain, searchId)

        then:
            result != null
            result.name == searchMovie
            result.year == searchYear

        where:
            movie1 = "Lord of the Rings"
            year1 = 2001

            movie2 = "Star Wars"
            year2 = 1977

            movie3 = "Matrix"
            year3 = 1999

            searchId << [ 1, 2, 3 ]
            searchMovie << [ "Lord of the Rings", "Star Wars", "Matrix" ]
            searchYear << [ 2001, 1977, 1999 ]
    }

    void "Save object"() {
        setup:
            mockDomain(TestDomain)

        when:
            def result = service.saveDomain(TestDomain, ['name': 'Silence of the Lambs', 'year': 1991])
            def find = TestDomain.findByName('Silence of the Lambs')

        then:
            result != null
            find != null
            result.name == find.name
            result.year == find.year
    }

    void "Save object, fail validation"() {
        setup:
            mockDomain(TestDomain)

        when:
            def result = service.saveDomain(TestDomain, ['name': null, 'year': 1991])

        then:
            thrown(RuntimeException)
    }

    void "Update object"() {
        setup:
            mockDomain(TestDomain,[
                [id: 1, name: 'The Matrix', year: 2001]])

        when:
            def result = service.updateDomain(TestDomain, 1, ['name':'The Matrix', 'year': 2014])
            def find = TestDomain.get(1)

        then:
            result != null
            find != null
            result.name == find.name
            result.year == find.year
    }

    void "Update object, object doesn't exist"() {
        setup:
            mockDomain(TestDomain,[
                [id: 1, name: 'The Matrix', year: 2001]])

        when:
            def result = service.updateDomain(TestDomain, 2, ['year': 2014])

        then:
            thrown(RuntimeException)
    }

    void "Update object, validation error"() {
        setup:
            mockDomain(TestDomain,[
                [id: 1, name: 'The Matrix', year: 2001]])

        when:
            def result = service.updateDomain(TestDomain, 1, ['name': null])

        then:
            thrown(RuntimeException)
    }

    void "Remove object"() {
        setup:
            mockDomain(TestDomain,[
                [id: 1, name: 'The Matrix', year: 2001]])

        when:
            def result = service.deleteDomain(TestDomain, 1)
            def find = TestDomain.findByName('The Matrix')

        then:
            result == true
            find == null
    }

    void "Remove a non existant object"() {
        setup:
            mockDomain(TestDomain,[
                [id: 1, name: 'The Matrix', year: 2001]])

        when:
            def result = service.deleteDomain(TestDomain, 3)

        then:
            result == false

    }

    void "Return list objects"() {
        setup:
            mockDomain(TestDomain,[
                           [id: 1, name: 'The Matrix', year: 2001],
                           [id: 2, name: 'The Matrix 2', year: 2002],
                           [id: 3, name: 'The Matrix 3', year: 2003],
                           [id: 4, name: 'The Matrix 5', year: 2004]])

        when:
            def result = service.list(TestDomain)
            def resultoff = service.list(TestDomain, offset, limit, sort, order)

        then:
            result.size() == size
            resultoff.size() == resultsize
            resultoff[0].id == firstId

        where:
           size = 4
           resultsize | limit | offset | firstId | sort   | order
           3          |     3 |      0 |       1 | 'year' | 'asc'
           1          |     3 |      3 |       4 | 'year' | 'asc'
           3          |     3 |      0 |       4 | 'year' | 'desc'

    }

    void "Return the number of total objects"() {
        setup:
            mockDomain(TestDomain,[
                           [id: 1, name: 'The Matrix', year: 2001],
                           [id: 2, name: 'The Matrix 2', year: 2002],
                           [id: 3, name: 'The Matrix 3', year: 2002],
                           [id: 4, name: 'The Matrix 5', year: 2002]])

        when:
            def result = service.count(TestDomain)
        then:
            result == 4
    }


    void "Remove related object"() {
        setup:

            def grailsAdminPluginWidgetService = Mock(GrailsAdminPluginWidgetService)
            grailsAdminPluginWidgetService.getGrailsDomainClass(_) >> { return new DefaultGrailsDomainClass(TestDomainRelation.class) }
            service.grailsAdminPluginWidgetService = grailsAdminPluginWidgetService



            def td = new TestDomain(id: 1, name: 'The Matrix', year: 2001)
            td.save()
            def rel = new TestDomainRelation()
            rel.save()

            rel.addToTestDomain(td)

            assert rel.testDomain.size() == 1

        when:
            service.deleteRelatedDomain(TestDomainRelation, rel.id, "testDomain", td.id)


        then:
            rel.testDomain.size() == 0
    }
}
