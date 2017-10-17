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

/** @module massis3-services-js/simulation_server_service */
var utils = require('vertx-js/util/utils');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JSimulationServerService = Java.type('com.massisframework.massis3.services.eventbus.SimulationServerService');

/**
 @class
*/
var SimulationServerService = function(j_val) {

  var j_simulationServerService = j_val;
  var that = this;

  /**
   Retrieves the current active simulations

   @public
   @param resultHandler {function} 
   */
  this.activeSimulations = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_simulationServerService["activeSimulations(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Retrieves the current available simulation scenes

   @public
   @param resultHandler {function} 
   */
  this.availableScenes = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_simulationServerService["availableScenes(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Creates a simulation

   @public
   @param sceneFile {string} the simulation scene file 
   @param resultHandler {function} 
   */
  this.create = function(sceneFile, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
      j_simulationServerService["create(java.lang.String,io.vertx.core.Handler)"](sceneFile, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnLong(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Destroys (stops and undeploy) a running simulation)

   @public
   @param simId {number} the simulation id 
   @param resultHandler {function} 
   */
  this.destroy = function(simId, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] ==='number' && typeof __args[1] === 'function') {
      j_simulationServerService["destroy(long,io.vertx.core.Handler)"](simId, function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_simulationServerService;
};

SimulationServerService._jclass = utils.getJavaClass("com.massisframework.massis3.services.eventbus.SimulationServerService");
SimulationServerService._jtype = {
  accept: function(obj) {
    return SimulationServerService._jclass.isInstance(obj._jdel);
  },
  wrap: function(jdel) {
    var obj = Object.create(SimulationServerService.prototype, {});
    SimulationServerService.apply(obj, arguments);
    return obj;
  },
  unwrap: function(obj) {
    return obj._jdel;
  }
};
SimulationServerService._create = function(jdel) {
  var obj = Object.create(SimulationServerService.prototype, {});
  SimulationServerService.apply(obj, arguments);
  return obj;
}
module.exports = SimulationServerService;