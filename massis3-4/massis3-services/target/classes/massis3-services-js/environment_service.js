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
var utils = require('vertx-js/util/utils');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JEnvironmentService = Java.type('com.massisframework.massis3.services.eventbus.sim.EnvironmentService');
var JsonPoint = Java.type('com.massisframework.massis3.services.dataobjects.JsonPoint');
var JsonQuaternion = Java.type('com.massisframework.massis3.services.dataobjects.JsonQuaternion');

/**
 @class
*/
var EnvironmentService = function(j_val) {

  var j_environmentService = j_val;
  var that = this;

  /**
   Retrieves the id of the areas (rooms) of the simulation environment

   @public
   @param resultHandler {function} 
   */
  this.roomIds = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_environmentService["roomIds(io.vertx.core.Handler)"](function(ar) {
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
   @param resultHandler {function} 
   */
  this.allRoomsInfo = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_environmentService["allRoomsInfo(io.vertx.core.Handler)"](function(ar) {
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
   @param id {number} the id og f the room 
   @param resultHandler {function} the information of the room, following the schema: 
   */
  this.roomInfo = function(id, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] ==='number' && typeof __args[1] === 'function') {
      j_environmentService["roomInfo(long,io.vertx.core.Handler)"](id, function(ar) {
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
   @param name {string} the name of the entity to be searched 
   @param resultHandler {function} A json array of the simulation entities that have the name provided. The entities are returned as their id. 
   */
  this.entitiesNamed = function(name, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
      j_environmentService["entitiesNamed(java.lang.String,io.vertx.core.Handler)"](name, function(ar) {
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
   @param id {number} the id of the entity 
   @param resultHandler {function} the location of the entity 
   */
  this.entityLocation = function(id, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] ==='number' && typeof __args[1] === 'function') {
      j_environmentService["entityLocation(long,io.vertx.core.Handler)"](id, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnDataObject(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
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
      j_environmentService["time(io.vertx.core.Handler)"](function(ar) {
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
   @param resultHandler {function} 
   */
  this.timeStreamAddress = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_environmentService["timeStreamAddress(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
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
      j_environmentService["sceneName(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
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
      j_environmentService["addCamera(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
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
      j_environmentService["setCameraLocation(java.lang.String,com.massisframework.massis3.services.dataobjects.JsonPoint,io.vertx.core.Handler)"](camId, location != null ? new JsonPoint(new JsonObject(Java.asJSONCompatible(location))) : null, function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
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
      j_environmentService["setCameraRotation(java.lang.String,com.massisframework.massis3.services.dataobjects.JsonQuaternion,io.vertx.core.Handler)"](camId, rotation != null ? new JsonQuaternion(new JsonObject(Java.asJSONCompatible(rotation))) : null, function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
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
      j_environmentService["setCameraRotationWithAngles(java.lang.String,float,float,float,io.vertx.core.Handler)"](camId, xAngle, yAngle, zAngle, function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.cameraIds = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_environmentService["cameraIds(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
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
      j_environmentService["cameraMapKeyValue(java.lang.String,io.vertx.core.Handler)"](camId, function(ar) {
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
   @param camId {string} 
   @param handler {function} 
   */
  this.cameraDataStreamEventBusAddress = function(camId, handler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
      j_environmentService["cameraDataStreamEventBusAddress(java.lang.String,io.vertx.core.Handler)"](camId, function(ar) {
      if (ar.succeeded()) {
        handler(ar.result(), null);
      } else {
        handler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_environmentService;
};

EnvironmentService._jclass = utils.getJavaClass("com.massisframework.massis3.services.eventbus.sim.EnvironmentService");
EnvironmentService._jtype = {
  accept: function(obj) {
    return EnvironmentService._jclass.isInstance(obj._jdel);
  },
  wrap: function(jdel) {
    var obj = Object.create(EnvironmentService.prototype, {});
    EnvironmentService.apply(obj, arguments);
    return obj;
  },
  unwrap: function(obj) {
    return obj._jdel;
  }
};
EnvironmentService._create = function(jdel) {
  var obj = Object.create(EnvironmentService.prototype, {});
  EnvironmentService.apply(obj, arguments);
  return obj;
}
module.exports = EnvironmentService;