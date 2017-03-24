package com.blogspot.toomuchcoding.spock.subjcollabs

import groovy.transform.PackageScope
import org.codehaus.groovy.reflection.ClassInfo
import org.spockframework.runtime.model.FieldInfo

import java.lang.reflect.Field
import java.lang.reflect.Modifier
import static java.lang.reflect.Modifier.isFinal

@PackageScope
class FieldRetriever {

	Map<Field, Field> getAllUnsetFields(Collection<Field> injectionCandidates, FieldInfo fieldInfo, Object subject) {
		return getAllMatchingFields(injectionCandidates, fieldInfo, subject)
	}

	private Map<Field, Field> getAllMatchingFields(Collection<Field> injectionCandidates, FieldInfo fieldInfo, Object subject) {
		List<Field> fields = getAllFieldsFromSubject(fieldInfo.type, [])
		return getMatchingFieldsBasingOnTypeAndPropertyName(injectionCandidates, fields, subject)
	}

	private List<Field> getAllFieldsFromSubject(Class type, List<Field> fields) {
		fields.addAll(type.declaredFields.findAll { Field field -> !field.isSynthetic() && !isFinal(field.getModifiers()) && field.type != ClassInfo })
		if (type.superclass != null) {
			fields.addAll(getAllFieldsFromSubject(type.superclass, fields))
		}
		fields*.setAccessible(true)
		return fields
	}

	private Map getMatchingFieldsBasingOnTypeAndPropertyName(Collection<Field> injectionCandidates, List<Field> allFields, Object subject) {
		Map matchingFields = [:]
		injectionCandidates.each { Field injectionCandidate ->
			List<Field> matchingTypes = allFields.findAll { it.type == injectionCandidate.type }
			Field injectionCandidateByNameAndType = matchingTypes.find { it.name.equalsIgnoreCase(injectionCandidate.name) }
			if (injectionCandidateByNameAndType) {
				matchingFields[injectionCandidateByNameAndType] = injectionCandidate
			}
            else if(matchingTypes.size() > 1) {
                List<Field> nonUnSetMatchingTypes = matchingTypes.findAll { it.get(subject) == null }  
                if(nonUnSetMatchingTypes.isEmpty()) {
                    matchingFields = matchingFields << matchingTypes.collectEntries { [(it) : injectionCandidate] }
                }
                else {
                    matchingFields = matchingFields << nonUnSetMatchingTypes.collectEntries { [(it) : injectionCandidate] }
                }
            }
             else {
				matchingFields = matchingFields << matchingTypes.collectEntries { [(it) : injectionCandidate] }
			}
		}
		return matchingFields
	}
}
