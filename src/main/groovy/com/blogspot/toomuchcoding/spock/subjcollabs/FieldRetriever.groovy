package com.blogspot.toomuchcoding.spock.subjcollabs

import groovy.transform.PackageScope
import org.codehaus.groovy.reflection.ClassInfo
import org.spockframework.runtime.model.FieldInfo

import java.lang.reflect.Field

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
		fields.addAll(type.declaredFields.findAll { Field field -> !field.isSynthetic() && field.type != ClassInfo })
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
                        else {
                                List<Field> nullMatchingTypes = matchingTypes.findAll { it.get(subject) == null }
                                matchingFields = matchingFields << nullMatchingTypes.collectEntries { [(it) : injectionCandidate] }
                        }
		}
		return matchingFields
	}
}
