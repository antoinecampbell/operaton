/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.operaton.bpm.engine.rest.dto;

import java.util.Base64;
import org.operaton.bpm.engine.ProcessEngine;
import org.operaton.bpm.engine.rest.exception.InvalidRequestException;
import org.operaton.bpm.engine.rest.exception.RestException;
import org.operaton.bpm.engine.rest.mapper.MultipartFormData.FormPart;
import org.operaton.bpm.engine.variable.VariableMap;
import org.operaton.bpm.engine.variable.Variables;
import org.operaton.bpm.engine.variable.type.*;
import org.operaton.bpm.engine.variable.value.FileValue;
import org.operaton.bpm.engine.variable.value.SerializableValue;
import org.operaton.bpm.engine.variable.value.TypedValue;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author Daniel Meyer
 * @author Thorben Lindhauer
 *
 */
public class VariableValueDto {

  protected String type;
  protected Object value;
  protected Map<String, Object> valueInfo;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public Map<String, Object> getValueInfo() {
    return valueInfo;
  }

  public void setValueInfo(Map<String, Object> valueInfo) {
    this.valueInfo = valueInfo;
  }

  public TypedValue toTypedValue(ProcessEngine processEngine, ObjectMapper objectMapper) {
    ValueTypeResolver valueTypeResolver = processEngine.getProcessEngineConfiguration().getValueTypeResolver();

    if (type == null) {
      if (valueInfo != null && valueInfo.get(ValueType.VALUE_INFO_TRANSIENT) instanceof Boolean booleanValueInfo) {
        return Variables.untypedValue(value, booleanValueInfo);
      }
      return Variables.untypedValue(value);
    }

    ValueType valueType = valueTypeResolver.typeForName(fromRestApiTypeName(type));
    if(valueType == null) {
      throw new RestException(Status.BAD_REQUEST, String.format("Unsupported value type '%s'", type));
    }
    else {
      if(valueType instanceof PrimitiveValueType primitiveValueType) {
        Class<?> javaType = primitiveValueType.getJavaType();
        Object mappedValue = null;
        try {
          if(value != null) {
            if(javaType.isAssignableFrom(value.getClass())) {
              mappedValue = value;
            }
            else {
              // use jackson to map the value to the requested java type
              mappedValue = objectMapper.readValue("\""+value+"\"", javaType);
            }
          }
          return valueType.createValue(mappedValue, valueInfo);
        }
        catch (Exception e) {
          throw new InvalidRequestException(Status.BAD_REQUEST, e,
              String.format("Cannot convert value '%s' of type '%s' to java type %s", value, type, javaType.getName()));
        }
      }
      else if(valueType instanceof SerializableValueType serializableValueType) {
        if(value != null && !(value instanceof String)) {
          throw new InvalidRequestException(Status.BAD_REQUEST, "Must provide 'null' or String value for value of SerializableValue type '"+type+"'.");
        }
        return serializableValueType.createValueFromSerialized((String) value, valueInfo);
      }
      else if(valueType instanceof FileValueType) {

        if (value instanceof String stringValue) {
          value = Base64.getDecoder().decode(stringValue);
        }

        return valueType.createValue(value, valueInfo);
      } else {
        return valueType.createValue(value, valueInfo);
      }
    }

  }

  public static VariableMap toMap(Map<String, VariableValueDto> variables, ProcessEngine processEngine, ObjectMapper objectMapper) {
    if(variables == null) {
      return null;
    }

    VariableMap result = Variables.createVariables();
    for (Entry<String, VariableValueDto> variableEntry : variables.entrySet()) {
      result.put(variableEntry.getKey(), variableEntry.getValue().toTypedValue(processEngine, objectMapper));
    }

    return result;
  }

  public static Map<String, VariableValueDto> fromMap(VariableMap variables)
  {
    return fromMap(variables, false);
  }

  public static Map<String, VariableValueDto> fromMap(VariableMap variables, boolean preferSerializedValue)
  {
    Map<String, VariableValueDto> result = new HashMap<>();
    for (String variableName : variables.keySet()) {
      VariableValueDto valueDto = VariableValueDto.fromTypedValue(variables.getValueTyped(variableName), preferSerializedValue);
      result.put(variableName, valueDto);
    }

    return result;
  }

  public static VariableValueDto fromTypedValue(TypedValue typedValue) {
    VariableValueDto dto = new VariableValueDto();
    fromTypedValue(dto, typedValue);
    return dto;
  }

  public static VariableValueDto fromTypedValue(TypedValue typedValue, boolean preferSerializedValue) {
    VariableValueDto dto = new VariableValueDto();
    fromTypedValue(dto, typedValue, preferSerializedValue);
    return dto;
  }

  public static void fromTypedValue(VariableValueDto dto, TypedValue typedValue) {
    fromTypedValue(dto, typedValue, false);
  }

  public static void fromTypedValue(VariableValueDto dto, TypedValue typedValue, boolean preferSerializedValue) {

    ValueType type = typedValue.getType();
    if (type != null) {
      String typeName = type.getName();
      dto.setType(toRestApiTypeName(typeName));
      dto.setValueInfo(type.getValueInfo(typedValue));
    }

    if(typedValue instanceof SerializableValue serializableValue) {

      if(serializableValue.isDeserialized() && !preferSerializedValue) {
        dto.setValue(serializableValue.getValue());
      }
      else {
        dto.setValue(serializableValue.getValueSerialized());
      }

    }
    else if(typedValue instanceof FileValue){
      //do not set the value for FileValues since we don't want to send megabytes over the network without explicit request
    }
    else {
      dto.setValue(typedValue.getValue());
    }

  }

  public static String toRestApiTypeName(String name) {
    return name.substring(0, 1).toUpperCase() + name.substring(1);
  }

  public static String fromRestApiTypeName(String name) {
    return name.substring(0, 1).toLowerCase() + name.substring(1);
  }

  public static VariableValueDto fromFormPart(String type, FormPart binaryDataFormPart) {
    VariableValueDto dto = new VariableValueDto();

    dto.type = type;
    dto.value = binaryDataFormPart.getBinaryContent();

    if (ValueType.FILE.getName().equals(fromRestApiTypeName(type))) {

      String contentType = binaryDataFormPart.getContentType();
      if (contentType == null) {
        contentType = MediaType.APPLICATION_OCTET_STREAM;
      }

      dto.valueInfo = new HashMap<>();
      dto.valueInfo.put(FileValueType.VALUE_INFO_FILE_NAME, binaryDataFormPart.getFileName());
      MimeType mimeType = null;
      try {
        mimeType = new MimeType(contentType);
      } catch (MimeTypeParseException e) {
        throw new RestException(Status.BAD_REQUEST, "Invalid mime type given");
      }

      dto.valueInfo.put(FileValueType.VALUE_INFO_FILE_MIME_TYPE, mimeType.getBaseType());

      String encoding = mimeType.getParameter("encoding");
      if (encoding != null) {
        dto.valueInfo.put(FileValueType.VALUE_INFO_FILE_ENCODING, encoding);
      }

      String transientString = mimeType.getParameter("transient");
      boolean isTransient = Boolean.parseBoolean(transientString);
      if (isTransient) {
        dto.valueInfo.put(ValueType.VALUE_INFO_TRANSIENT, isTransient);
      }
    }

    return dto;


  }

}
