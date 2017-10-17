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
!function (factory) {
  if (typeof require === 'function' && typeof module !== 'undefined') {
    factory();
  } else if (typeof define === 'function' && define.amd) {
    // AMD loader
    define('massis3-services-js/echo_service-proxy', [], factory);
  } else {
    // plain old include
    EchoService = factory();
  }
}(function () {

  /**
 @class
  */
  var EchoService = function(eb, address) {

    var j_eb = eb;
    var j_address = address;
    var closed = false;
    var that = this;
    var convCharCollection = function(coll) {
      var ret = [];
      for (var i = 0;i < coll.length;i++) {
        ret.push(String.fromCharCode(coll[i]));
      }
      return ret;
    };

    /**

     @public
     @param message {string} 
     @param handler {function} 
     */
    this.echoString = function(message, handler) {
      var __args = arguments;
      if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"message":__args[0]}, {"action":"echoString"}, function(err, result) { __args[1](err, result &&result.body); });
        return;
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"message":__args[0]}, {"action":"echoInteger"}, function(err, result) { __args[1](err, result &&result.body); });
        return;
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"message":__args[0]}, {"action":"echoJsonObject"}, function(err, result) { __args[1](err, result &&result.body); });
        return;
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"message":__args[0]}, {"action":"echoJsonArray"}, function(err, result) { __args[1](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param handler {function} 
     */
    this.echoVoidNoParams = function(handler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"echoVoidNoParams"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"a":__args[0], "b":__args[1], "c":__args[2]}, {"action":"echoVoidWithParams"}, function(err, result) { __args[3](err, result &&result.body); });
        return;
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"fail":__args[0], "errMessage":__args[1]}, {"action":"echoCanFail"}, function(err, result) { __args[2](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param handler {function} 
     */
    this.echoException = function(handler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"echoException"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param handler {function} 
     */
    this.echoCounterStreamAddress = function(handler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"echoCounterStreamAddress"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public

     */
    this.close = function() {
      var __args = arguments;
      if (__args.length === 0) {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"close"});
        closed = true;
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

  };

  if (typeof exports !== 'undefined') {
    if (typeof module !== 'undefined' && module.exports) {
      exports = module.exports = EchoService;
    } else {
      exports.EchoService = EchoService;
    }
  } else {
    return EchoService;
  }
});