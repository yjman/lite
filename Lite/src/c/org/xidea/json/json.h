/*
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/lite/
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * @author jindw@xidea.org
 *
 */

#ifndef JSON_H
#define JSON_H
#include <stdio.h>

#ifdef __cplusplus
extern "C" {
#endif

#define JSON_BOOL 0
#define JSON_INT 1
#define JSON_FLOAT 2
#define JSON_STRING 3
#define JSON_ARRAY 4
#define JSON_OBJECT 5
#define JSON_NULL 6


typedef struct json_value {
	int type;
} json_value;

int json_get_type(struct json_value * thiz);

int json_get_length (struct json_value * thiz);

int json_get_bool (struct json_value * thiz);

int json_get_int (struct json_value * thiz);

float json_get_float (struct json_value * thiz);

char* json_get_string (struct json_value * thiz);

struct json_value* json_get_by_key (struct json_value * thiz,const char* key);

void json_set_by_key (struct json_value * thiz,const char* key, struct json_value * value);

void json_remove_by_key (struct json_value * thiz,const char* key);

struct json_value* json_get_by_index (struct json_value * thiz,int index);

void json_set_by_index (struct json_value * thiz,int index,struct json_value * value);

void json_remove_by_index (struct json_value * thiz,const char* key);

void json_add_value (struct json_value * thiz,struct json_value * value);

struct json_value* json_create_bool (const int value);

struct json_value* json_create_int (const int value);

struct json_value* json_create_float (const float value);

struct json_value* json_create_string (const char* value);

struct json_value* json_create_array ();

struct json_value* json_create_object ();

struct json_value* json_create_null ();

int json_free(struct json_value * value,const int force);

#ifdef __cplusplus
}
#endif
#endif
