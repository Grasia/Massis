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
!function (factory) {
  if (typeof require === 'function' && typeof module !== 'undefined') {
    factory();
  } else if (typeof define === 'function' && define.amd) {
    // AMD loader
    define('massis3-services-js/simulation_server_service-proxy', [], factory);
  } else {
    // plain old include
    SimulationServerService = factory();
  }
}(function () {

  /**
 @class
  */
  var SimulationServerService = function(eb, address) {

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
     Retrieves the current active simulations

     @public
     @param resultHandler {function} 
     */
    this.activeSimulations = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"activeSimulations"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"availableScenes"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"sceneFile":__args[0]}, {"action":"create"}, function(err, result) { __args[1](err, result &&result.body); });
        return;
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"simId":__args[0]}, {"action":"destroy"}, function(err, result) { __args[1](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

  };

  if (typeof exports !== 'undefined') {
    if (typeof module !== 'undefined' && module.exports) {
      exports = module.exports = SimulationServerService;
    } else {
      exports.SimulationServerService = SimulationServerService;
    }
  } else {
    return SimulationServerService;
  }
});