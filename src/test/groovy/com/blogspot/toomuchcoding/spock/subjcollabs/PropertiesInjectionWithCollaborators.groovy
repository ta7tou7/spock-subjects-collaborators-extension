package com.blogspot.toomuchcoding.spock.subjcollabs

import spock.lang.Specification

class PropertiesInjectionWithCollaborators extends Specification
{
    public static final String TEST_COLLABORATOR_METHOD = "Test collaborator method"
    
    @Collaborator
    SomeOtherClass someOtherClass = Mock()
    @Subject
    SomeClass someClass

    def 'when matching by name and type collaborate object should be injected' ()
    {
        given:
        someOtherClass.someMethod() >> TEST_COLLABORATOR_METHOD
        when :
        String result = someClass.someOtherClass.someMethod()
        then :
        result == TEST_COLLABORATOR_METHOD
        someClass.someOtherClass == someOtherClass
    }
}
    class SomeClass
    {
        SomeOtherClass someOtherClass = new SomeOtherClass()

        SomeClass(SomeOtherClass someOtherClass)
        {
            this.someOtherClass = someOtherClass
        }
    }
    class SomeOtherClass
    {
        String someMethod()
        {
            "Test real method"
        }
    }
