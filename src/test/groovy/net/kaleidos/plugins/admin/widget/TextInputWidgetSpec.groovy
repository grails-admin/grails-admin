package net.kaleidos.plugins.admin.widget

import grails.plugin.spock.*
import spock.lang.*

import org.codehaus.groovy.grails.plugins.codecs.HTMLCodec


class TextInputWidgetSpec extends Specification {
    @Shared
    def slurper

    void setupSpec() {
        def parser = new org.cyberneko.html.parsers.SAXParser()
        parser.setFeature('http://xml.org/sax/features/namespaces', false)
        slurper = new XmlSlurper(parser)
    }

    void setup() {
        Object.metaClass.encodeAsHTML = {
            def encoder = new HTMLCodec().getEncoder()
            return encoder.encode(delegate)
        }
    }

    void 'create input text without value nor attribs'() {
        setup:
            def textInputWidget = new TextInputWidget()

        when:
            def html = textInputWidget.render()
            def result = slurper.parseText(html)

        then:
            result.BODY.INPUT.size() == 1
            result.BODY.INPUT.@type.text() == "text"
            result.BODY.INPUT.@value.text() == ""
    }

    void 'create input text with value without attribs'() {
        setup:
            def textInputWidget = new TextInputWidget(value:value)

        when:
            def html = textInputWidget.render()
            def result = slurper.parseText(html)

        then:
            result.BODY.INPUT.size() == 1
            result.BODY.INPUT.@type.text() == "text"
            result.BODY.INPUT.@value.text() == value

        where:
            value = "<script>alert(1234)</script>"
    }

    void 'create input text without value with attribs'() {
        setup:
            def textInputWidget = new TextInputWidget(htmlAttrs:attrs)

        when:
            def html = textInputWidget.render()
            def result = slurper.parseText(html)

        then:
            result.BODY.INPUT.size() == 1
            result.BODY.INPUT.@type.text() == "text"
            result.BODY.INPUT.@size.text() == "10"
            result.BODY.INPUT.@name.text() == "test"
            result.BODY.INPUT.@value.text() == ""

        where:
            attrs = ['size':10, 'name': 'test']
    }

    void 'create input text with value and attribs'() {
        setup:
            def textInputWidget = new TextInputWidget(value:value, htmlAttrs:attrs)

        when:
            def html = textInputWidget.render()
            def result = slurper.parseText(html)

        then:
            result.BODY.INPUT.size() == 1
            result.BODY.INPUT.@type.text() == "text"
            result.BODY.INPUT.@size.text() == "10"
            result.BODY.INPUT.@name.text() == "test"
            result.BODY.INPUT.@value.text() == value

        where:
            value = "<script>alert(1234)</script>"
            attrs = ['size':10, 'name': 'test']
    }
}
