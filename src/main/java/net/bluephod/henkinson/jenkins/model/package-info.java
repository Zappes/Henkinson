/**
 * Model classes for the Jenkins API.
 *
 * The general idea for parsing the API is quite simple: The JSON version of an API object is retrieved and it is deserialized into a
 * Java object using Jackson's <code>ObjectMapper</code>. The interesting part is that the annotation <code>@JsonIgnoreProperties
 * (ignoreUnknown=true)</code> has been put on all classes, thus making it possible to deserialize only the data one is interested in
 * while ignoring everything else.
 *
 * If you write some extension to the code that requires more information from the API, no problem. Just add the corresponding properties
 * to the API class and it will be deserialized automatically.
 */
package net.bluephod.henkinson.jenkins.model;
