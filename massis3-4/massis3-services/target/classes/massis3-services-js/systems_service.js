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

/** @module massis3-services-js/systems_service */
var utils = require('vertx-js/util/utils');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JSystemsService = Java.type('com.massisframework.massis3.services.eventbus.sim.SystemsService');

/**
 @class
*/
var SystemsService = function(j_val) {

  var j_systemsService = j_val;
  var that = this;

  /**

   @public
   @param resultHandler {function} 
   */
  this.getRunningSystems = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_systemsService["getRunningSystems(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param systemName {string} 
   @param resultHandler {function} 
   */
  this.systemStatus = function(systemName, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
      j_systemsService["systemStatus(java.lang.String,io.vertx.core.Handler)"](systemName, function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param systemName {string} 
   @param resultHandler {function} 
   */
  this.disableSystem = function(systemName, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
      j_systemsService["disableSystem(java.lang.String,io.vertx.core.Handler)"](systemName, function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param systemName {string} 
   @param resultHandler {function} 
   */
  this.enableSystem = function(systemName, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
      j_systemsService["enableSystem(java.lang.String,io.vertx.core.Handler)"](systemName, function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_systemsService;
};

SystemsService._jclass = utils.getJavaClass("com.massisframework.massis3.services.eventbus.sim.SystemsService");
SystemsService._jtype = {
  accept: function(obj) {
    return SystemsService._jclass.isInstance(obj._jdel);
  },
  wrap: function(jdel) {
    var obj = Object.create(SystemsService.prototype, {});
    SystemsService.apply(obj, arguments);
    return obj;
  },
  unwrap: function(obj) {
    return obj._jdel;
  }
};
SystemsService._create = function(jdel) {
  var obj = Object.create(SystemsService.prototype, {});
  SystemsService.apply(obj, arguments);
  return obj;
}
module.exports = SystemsService;