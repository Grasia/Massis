/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

/** @module massis3-services-js/echo_service */
var utils = require('vertx-js/util/utils');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JEchoService = Java.type('com.massisframework.massis3.services.eventbus.EchoService');

/**
 @class
*/
var EchoService = function(j_val) {

  var j_echoService = j_val;
  var that = this;

  /**

   @public
   @param message {string} 
   @param handler {function} 
   */
  this.echoString = function(message, handler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
      j_echoService["echoString(java.lang.String,io.vertx.core.Handler)"](message, function(ar) {
      if (ar.succeeded()) {
        handler(ar.result(), null);
      } else {
        handler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param message {number} 
   @param handler {function} 
   */
  this.echoInteger = function(message, handler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] ==='number' && typeof __args[1] === 'function') {
      j_echoService["echoInteger(java.lang.Integer,io.vertx.core.Handler)"](utils.convParamInteger(message), function(ar) {
      if (ar.succeeded()) {
        handler(ar.result(), null);
      } else {
        handler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param message {Object} 
   @param handler {function} 
   */
  this.echoJsonObject = function(message, handler) {
    var __args = arguments;
    if (__args.length === 2 && (typeof __args[0] === 'object' && __args[0] != null) && typeof __args[1] === 'function') {
      j_echoService["echoJsonObject(io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](utils.convParamJsonObject(message), function(ar) {
      if (ar.succeeded()) {
        handler(utils.convReturnJson(ar.result()), null);
      } else {
        handler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param message {todo} 
   @param handler {function} 
   */
  this.echoJsonArray = function(message, handler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'object' && __args[0] instanceof Array && typeof __args[1] === 'function') {
      j_echoService["echoJsonArray(io.vertx.core.json.JsonArray,io.vertx.core.Handler)"](utils.convParamJsonArray(message), function(ar) {
      if (ar.succeeded()) {
        handler(utils.convReturnJson(ar.result()), null);
      } else {
        handler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param handler {function} 
   */
  this.echoVoidNoParams = function(handler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_echoService["echoVoidNoParams(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        handler(null, null);
      } else {
        handler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param a {string} 
   @param b {string} 
   @param c {number} 
   @param handler {function} 
   */
  this.echoVoidWithParams = function(a, b, c, handler) {
    var __args = arguments;
    if (__args.length === 4 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && typeof __args[2] ==='number' && typeof __args[3] === 'function') {
      j_echoService["echoVoidWithParams(java.lang.String,java.lang.String,java.lang.Integer,io.vertx.core.Handler)"](a, b, utils.convParamInteger(c), function(ar) {
      if (ar.succeeded()) {
        handler(null, null);
      } else {
        handler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param fail {boolean} 
   @param errMessage {string} 
   @param handler {function} 
   */
  this.echoCanFail = function(fail, errMessage, handler) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] ==='boolean' && typeof __args[1] === 'string' && typeof __args[2] === 'function') {
      j_echoService["echoCanFail(boolean,java.lang.String,io.vertx.core.Handler)"](fail, errMessage, function(ar) {
      if (ar.succeeded()) {
        handler(ar.result(), null);
      } else {
        handler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param handler {function} 
   */
  this.echoException = function(handler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_echoService["echoException(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        handler(null, null);
      } else {
        handler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param handler {function} 
   */
  this.echoCounterStreamAddress = function(handler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_echoService["echoCounterStreamAddress(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        handler(ar.result(), null);
      } else {
        handler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   */
  this.close = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_echoService["close()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_echoService;
};

EchoService._jclass = utils.getJavaClass("com.massisframework.massis3.services.eventbus.EchoService");
EchoService._jtype = {
  accept: function(obj) {
    return EchoService._jclass.isInstance(obj._jdel);
  },
  wrap: function(jdel) {
    var obj = Object.create(EchoService.prototype, {});
    EchoService.apply(obj, arguments);
    return obj;
  },
  unwrap: function(obj) {
    return obj._jdel;
  }
};
EchoService._create = function(jdel) {
  var obj = Object.create(EchoService.prototype, {});
  EchoService.apply(obj, arguments);
  return obj;
}
module.exports = EchoService;