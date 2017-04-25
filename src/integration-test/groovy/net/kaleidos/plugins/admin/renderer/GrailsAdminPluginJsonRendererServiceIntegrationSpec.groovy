package net.kaleidos.plugins.admin.renderer

import admin.test.AdminDomainTest
import grails.converters.JSON
import grails.util.Holders
import spock.lang.Specification


class GrailsAdminPluginJsonRendererServiceIntegrationSpec extends Specification {
    def grailsAdminPluginJsonRendererService
    def adminConfigHolder
    def slurper

    def setup() {
        Holders.config = new ConfigObject()
        Holders.config.grails.plugin.admin.domains = [ "admin.test.AdminDomainTest" ]

        adminConfigHolder.initialize()
    }

    void 'render single object as json'() {
        given: 'an object'
            def adminDomainTest = new AdminDomainTest(id:1, name:'a')

        when: 'ask to render as json'
            def json = grailsAdminPluginJsonRendererService.renderObjectAsJson(adminDomainTest)
            def result = JSON.parse(json)

        then: 'should return a json response'
            result.size() == 16
            result['id'] != null
            result['__text__'] != null
    }

    void 'render list as json'() {
        given: 'an list object'
            def list = [
                    new AdminDomainTest(id:1, name:'Paul1'),
                    new AdminDomainTest(id:2, name:'Paul2'),
                    new AdminDomainTest(id:3, name:'Paul3'),
                    new AdminDomainTest(id:4, name:'Paul4')
                ]

        when: 'ask to render list as json'
            def json = grailsAdminPluginJsonRendererService.renderListAsJson(list)
            def result = JSON.parse(json)

        then: 'should return a json response'
            result.size() == 4
            result.each {
                it.size() == 14
            }
    }
}
