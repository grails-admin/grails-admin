package net.kaleidos.plugins.admin.config

import admin.test.TestExtendsDomain
import grails.core.DefaultGrailsApplication
import grails.core.GrailsApplication
import grails.util.Holders
import spock.lang.Specification

class AdminConfigHolderSpec extends Specification {
	void setup() {
		GrailsApplication grailsApplication = new DefaultGrailsApplication()
		grailsApplication.configureLoadedClasses([
				admin.test.TestDomain.class,
				admin.test.TestOtherDomain.class,
				admin.test.TestExtendsDomain.class
		] as Class[])
		Holders.grailsApplication = grailsApplication
	}

	void "Obtain domain configuration - one"() {
		setup: "configuration"
		Holders.config = new ConfigObject()
		Holders.config.grails.plugin.admin.domains = testDomains

		and: "Config holder"
		def configHolder = new AdminConfigHolder()

		when:
		configHolder.initialize()

		then:
		configHolder.domains != null
		configHolder.domains.size() == 2

		where:
		testDomains = [
				"admin.test.TestDomain",
				"admin.test.TestOtherDomain"
		]
	}

	void "Obtain domain configuration"() {
		setup: "configuration"
		Holders.config = new ConfigObject()
		Holders.config.grails.plugin.admin.domains.group1 = testDomainsGroup1
		Holders.config.grails.plugin.admin.domains.group2 = testDomainsGroup2

		and: "Config holder"
		def configHolder = new AdminConfigHolder()

		when:
		configHolder.initialize()

		then:
		configHolder.domains != null
		configHolder.domains.size() == 2
		configHolder.groups != null
		configHolder.groups.size() == 2
		configHolder.groups.containsAll(["group1", "group2"])

		configHolder.getGroup("group1") != null
		configHolder.getGroup("group1").size() == 1

		configHolder.getGroup("group2") != null
		configHolder.getGroup("group2").size() == 1

		where:
		testDomainsGroup1 = [
				"admin.test.TestDomain"
		]

		testDomainsGroup2 = [
				"admin.test.TestOtherDomain"
		]
	}

	void "Obtain configuration (Domain closure configuration)"() {
		setup: "configuration"
		Holders.config = new ConfigObject()
		Holders.config.grails.plugin.admin.domains = [
				"admin.test.TestDomain"
		]
		Holders.config.grails.plugin.admin.domain.TestDomain = {
			list excludes: ['name', 'year']
		}

		and: "Config holder"
		def configHolder = new AdminConfigHolder()

		when:
		configHolder.initialize()

		then:
		configHolder.domains != null
		configHolder.domains.size() == 1
		configHolder.domains["admin.test.TestDomain"] != null
		configHolder.domains["admin.test.TestDomain"].getExcludes('list') != null
		configHolder.domains["admin.test.TestDomain"].getExcludes('list').size() == 2

	}

	void "Obtain configuration (Domain admin file)"() {
		setup: "configuration"
		Holders.config = new ConfigObject()
		Holders.config.grails.plugin.admin.domains = ["admin.test.TestDomain"]
		Holders.config.grails.plugin.admin.domain.TestDomain = "admin.test.TestDomainAdmin"

		and: "Config holder"
		def configHolder = new AdminConfigHolder()

		when:
		configHolder.initialize()

		then:
		configHolder.domains != null
		configHolder.domains.size() == 1
		configHolder.domains["admin.test.TestDomain"] != null
		configHolder.domains["admin.test.TestDomain"].getExcludes('list') != null
		configHolder.domains["admin.test.TestDomain"].getExcludes('list').size() == 2
	}

	void "Default configuration"() {
		setup: "configuration"
		Holders.config = new ConfigObject()

		when:
		def configHolder = new AdminConfigHolder()

		then:
		configHolder.domains != null
		configHolder.domains.size() == 0
	}

	void "Domain class doesn't exist"() {
		setup: "configuration"
		Holders.config = new ConfigObject()
		Holders.config.grails.plugin.admin.domains = ["NoExistsDomain"]

		and: "Config holder"
		def configHolder = new AdminConfigHolder()

		when:
		configHolder.initialize()

		then:
		thrown(RuntimeException)
	}

	def "Get all domain names"() {
		setup:
		Holders.config = new ConfigObject()
		Holders.config.grails.plugin.admin.domains = [
				"admin.test.TestDomain",
				"admin.test.TestOtherDomain"
		]

		and: "Config holder"
		def configHolder = new AdminConfigHolder()

		when:
		configHolder.initialize()
		def domainNames = configHolder.getDomainNames()

		then:
		domainNames == ["TestDomain", "TestOtherDomain"]
	}

	def "Get all lower domain names"() {
		setup:
		Holders.config = new ConfigObject()
		Holders.config.grails.plugin.admin.domains = [
				"admin.test.TestDomain",
				"admin.test.TestOtherDomain"
		]

		and: "Config holder"
		def configHolder = new AdminConfigHolder()

		when:
		configHolder.initialize()
		def domainNames = configHolder.slugDomainNames

		then:
		domainNames == ["testdomain", "testotherdomain"]
	}

	def "Given a slug, recover the DomainConfig object"() {
		setup:
		Holders.config = new ConfigObject()
		Holders.config.grails.plugin.admin.domains = testDomains

		and: "Config holder"
		def configHolder = new AdminConfigHolder()

		when:
		configHolder.initialize()
		DomainConfig testDomainConfig = configHolder.getDomainConfigBySlug(testDomainSlug)

		then:
		testDomainConfig == configHolder.domains[testDomainClassName]

		where:
		testDomains = ["admin.test.TestDomain"]
		testDomainSlug = "testdomain"
		testDomainClassName = "admin.test.TestDomain"
	}

	def "Given a non existant slug, don't recover any DomainConfig object"() {
		setup:
		Holders.config = new ConfigObject()
		Holders.config.grails.plugin.admin.domains = testDomains

		and: "Config holder"
		def configHolder = new AdminConfigHolder()

		when:
		configHolder.initialize()
		DomainConfig testDomainConfig = configHolder.getDomainConfigBySlug(testDomainSlug)

		then:
		testDomainConfig == null

		where:
		testDomains = ["admin.test.TestDomain"]
		testDomainSlug = "testdomainBadSlug"
		testDomainClassName = "admin.test.TestDomain"
	}

	def "Get the domain config of a super class"() {
		setup:
		Holders.config = new ConfigObject()
		Holders.config.grails.plugin.admin.domains = testDomains

		and: "Config holder"
		def configHolder = new AdminConfigHolder()
		def obj = new TestExtendsDomain()

		when:
		configHolder.initialize()
		DomainConfig testDomainConfig = configHolder.getDomainConfig(obj)

		then:
		testDomainConfig != null

		where:
		testDomains = ["admin.test.TestDomain"]
	}

	def "Get the domain for null"() {
		setup:
		Holders.config = new ConfigObject()
		Holders.config.grails.plugin.admin.domains = testDomains

		and: "Config holder"
		def configHolder = new AdminConfigHolder()
		def obj = new TestExtendsDomain()

		when:
		configHolder.initialize()
		def testDomainConfig = configHolder.getDomainConfig(null)

		then:
		testDomainConfig == null

		where:
		testDomains = ["admin.test.TestDomain"]
	}
}
