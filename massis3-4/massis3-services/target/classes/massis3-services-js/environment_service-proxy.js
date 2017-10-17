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

/** @module massis3-services-js/environment_service */
!function (factory) {
  if (typeof require === 'function' && typeof module !== 'undefined') {
    factory();
  } else if (typeof define === 'function' && define.amd) {
    // AMD loader
    define('massis3-services-js/environment_service-proxy', [], factory);
  } else {
    // plain old include
    EnvironmentService = factory();
  }
}(function () {

  /**
 @class
  */
  var EnvironmentService = function(eb, address) {

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
     Retrieves the id of the areas (rooms) of the simulation environment

     @public
     @param resultHandler {function} 
     */
    this.roomIds = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"roomIds"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.allRoomsInfo = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"allRoomsInfo"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param id {number} the id og f the room 
     @param resultHandler {function} the information of the room, following the schema: 
     */
    this.roomInfo = function(id, resultHandler) {
      var __args = arguments;
      if (__args.length === 2 && typeof __args[0] ==='number' && typeof __args[1] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"id":__args[0]}, {"action":"roomInfo"}, function(err, result) { __args[1](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param name {string} the name of the entity to be searched 
     @param resultHandler {function} A json array of the simulation entities that have the name provided. The entities are returned as their id. 
     */
    this.entitiesNamed = function(name, resultHandler) {
      var __args = arguments;
      if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"name":__args[0]}, {"action":"entitiesNamed"}, function(err, result) { __args[1](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param id {number} the id of the entity 
     @param resultHandler {function} the location of the entity 
     */
    this.entityLocation = function(id, resultHandler) {
      var __args = arguments;
      if (__args.length === 2 && typeof __args[0] ==='number' && typeof __args[1] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"id":__args[0]}, {"action":"entityLocation"}, function(err, result) { __args[1](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**
     Retrieves the simulation time, in seconds. Note that the time returned
     might not be <i>real</i> time: can be faster or slower.

     @public
     @param resultHandler {function} 
     */
    this.time = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"time"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.timeStreamAddress = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"timeStreamAddress"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**
     Returns the name of the simulation scene

     @public
     @param resultHandler {function} 
     */
    this.sceneName = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"sceneName"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**
     Attachs a camera to the simulation. Returns the camera id.

     @public
     @param resultHandler {function} 
     */
    this.addCamera = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"addCamera"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**
     Moves a camera

     @public
     @param camId {string} 
     @param location {Object} 
     @param resultHandler {function} 
     */
    this.setCameraLocation = function(camId, location, resultHandler) {
      var __args = arguments;
      if (__args.length === 3 && typeof __args[0] === 'string' && (typeof __args[1] === 'object' && __args[1] != null) && typeof __args[2] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"camId":__args[0], "location":__args[1]}, {"action":"setCameraLocation"}, function(err, result) { __args[2](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**
     Moves a camera

     @public
     @param camId {string} 
     @param rotation {Object} 
     @param resultHandler {function} 
     */
    this.setCameraRotation = function(camId, rotation, resultHandler) {
      var __args = arguments;
      if (__args.length === 3 && typeof __args[0] === 'string' && (typeof __args[1] === 'object' && __args[1] != null) && typeof __args[2] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"camId":__args[0], "rotation":__args[1]}, {"action":"setCameraRotation"}, function(err, result) { __args[2](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param camId {string} 
     @param xAngle {number} 
     @param yAngle {number} 
     @param zAngle {number} 
     @param resultHandler {function} 
     */
    this.setCameraRotationWithAngles = function(camId, xAngle, yAngle, zAngle, resultHandler) {
      var __args = arguments;
      if (__args.length === 5 && typeof __args[0] === 'string' && typeof __args[1] ==='number' && typeof __args[2] ==='number' && typeof __args[3] ==='number' && typeof __args[4] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"camId":__args[0], "xAngle":__args[1], "yAngle":__args[2], "zAngle":__args[3]}, {"action":"setCameraRotationWithAngles"}, function(err, result) { __args[4](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param resultHandler {function} 
     */
    this.cameraIds = function(resultHandler) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"cameraIds"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**
     Returns a key-value pair of the local map holding the camera data image
     data as jpg.

     @public
     @param camId {string} 
     @param resultHandler {function} 
     */
    this.cameraMapKeyValue = function(camId, resultHandler) {
      var __args = arguments;
      if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"camId":__args[0]}, {"action":"cameraMapKeyValue"}, function(err, result) { __args[1](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param camId {string} 
     @param handler {function} 
     */
    this.cameraDataStreamEventBusAddress = function(camId, handler) {
      var __args = arguments;
      if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"camId":__args[0]}, {"action":"cameraDataStreamEventBusAddress"}, function(err, result) { __args[1](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

  };

  if (typeof exports !== 'undefined') {
    if (typeof module !== 'undefined' && module.exports) {
      exports = module.exports = EnvironmentService;
    } else {
      exports.EnvironmentService = EnvironmentService;
    }
  } else {
    return EnvironmentService;
  }
});