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

/** @module massis3-services-js/human_agent_service */
var utils = require('vertx-js/util/utils');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JHumanAgentService = Java.type('com.massisframework.massis3.services.eventbus.sim.HumanAgentService');
var JsonPoint = Java.type('com.massisframework.massis3.services.dataobjects.JsonPoint');

/**
 @class
*/
var HumanAgentService = function(j_val) {

  var j_humanAgentService = j_val;
  var that = this;

  /**
   Creates a human in the specified location [x,y,z]

   @public
   @param location {Object} 
   @param resultHandler {function} 
   */
  this.createHuman = function(location, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && (typeof __args[0] === 'object' && __args[0] != null) && typeof __args[1] === 'function') {
      j_humanAgentService["createHuman(com.massisframework.massis3.services.dataobjects.JsonPoint,io.vertx.core.Handler)"](location != null ? new JsonPoint(new JsonObject(Java.asJSONCompatible(location))) : null, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnLong(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param humanId {number} 
   @param resultHandler {function} 
   */
  this.destroyHuman = function(humanId, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] ==='number' && typeof __args[1] === 'function') {
      j_humanAgentService["destroyHuman(java.lang.Long,io.vertx.core.Handler)"](utils.convParamLong(humanId), function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Returns the human ids of the simulation

   @public
   @param resultHandler {function} 
   */
  this.humanIds = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_humanAgentService["humanIds(io.vertx.core.Handler)"](function(ar) {
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
   @param name {string} 
   @param resultHandler {function} 
   */
  this.createHumanInArea = function(name, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
      j_humanAgentService["createHumanInArea(java.lang.String,io.vertx.core.Handler)"](name, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnLong(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Sets an animation to a human character. Note that the animation will be
   performed in loop mode.

   @public
   @param humanId {number} the id of the human character 
   @param animationName {string} the name of the animation. The extension <code>.massisanim</code> is not needed. 
   @param loop {boolean} if the animation should be executed in loop mode. If the value provided is <code>true</code>, the execution will return inmediately. Otherwise, this method will end once the animation cycle has ended. 
   @param resultHandler {function} result handler that will be called when the operation is completed. 
   */
  this.animate = function(humanId, animationName, loop, resultHandler) {
    var __args = arguments;
    if (__args.length === 4 && typeof __args[0] ==='number' && typeof __args[1] === 'string' && typeof __args[2] ==='boolean' && typeof __args[3] === 'function') {
      j_humanAgentService["animate(long,java.lang.String,boolean,io.vertx.core.Handler)"](humanId, animationName, loop, function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Retrieves the location of a human entity

   @public
   @param humanId {number} the entity id of the human 
   @param resultHandler {function} result handler that will be called when the operation is complete. 
   */
  this.getLocation = function(humanId, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] ==='number' && typeof __args[1] === 'function') {
      j_humanAgentService["getLocation(long,io.vertx.core.Handler)"](humanId, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnDataObject(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Checks if the human is following a path FIXME seems not to be working
   properly

   @public
   @param humanId {number} 
   @param resultHandler {function} 
   */
  this.isFollowingPath = function(humanId, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] ==='number' && typeof __args[1] === 'function') {
      j_humanAgentService["isFollowingPath(long,io.vertx.core.Handler)"](humanId, function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Tells the agent to move towards a point. Executes a pathfinding algorithm
   and follows a path

   @public
   @param humanId {number} the id of the human 
   @param target {Object} the target to follow, with three coordinates. 
   @param result {function} 
   */
  this.moveTowards = function(humanId, target, result) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] ==='number' && (typeof __args[1] === 'object' && __args[1] != null) && typeof __args[2] === 'function') {
      j_humanAgentService["moveTowards(long,com.massisframework.massis3.services.dataobjects.JsonPoint,io.vertx.core.Handler)"](humanId, target != null ? new JsonPoint(new JsonObject(Java.asJSONCompatible(target))) : null, function(ar) {
      if (ar.succeeded()) {
        result(null, null);
      } else {
        result(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Forces the agent to stop moving, if it was moving.

   @public
   @param humanId {number} the id of the agent 
   @param result {function} 
   */
  this.stopMoving = function(humanId, result) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] ==='number' && typeof __args[1] === 'function') {
      j_humanAgentService["stopMoving(long,io.vertx.core.Handler)"](humanId, function(ar) {
      if (ar.succeeded()) {
        result(ar.result(), null);
      } else {
        result(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Retrieves the agents in range.

   @public
   @param humanId {number} 
   @param range {number} 
   @param result {function} 
   */
  this.getHumanIdsInRange = function(humanId, range, result) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] ==='number' && typeof __args[1] ==='number' && typeof __args[2] === 'function') {
      j_humanAgentService["getHumanIdsInRange(long,float,io.vertx.core.Handler)"](humanId, range, function(ar) {
      if (ar.succeeded()) {
        result(utils.convReturnJson(ar.result()), null);
      } else {
        result(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param result {function} 
   */
  this.positionStreamingAddress = function(result) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_humanAgentService["positionStreamingAddress(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        result(ar.result(), null);
      } else {
        result(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param humanId {number} 
   @param result {function} 
   */
  this.humanExists = function(humanId, result) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] ==='number' && typeof __args[1] === 'function') {
      j_humanAgentService["humanExists(long,io.vertx.core.Handler)"](humanId, function(ar) {
      if (ar.succeeded()) {
        result(ar.result(), null);
      } else {
        result(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_humanAgentService;
};

HumanAgentService._jclass = utils.getJavaClass("com.massisframework.massis3.services.eventbus.sim.HumanAgentService");
HumanAgentService._jtype = {
  accept: function(obj) {
    return HumanAgentService._jclass.isInstance(obj._jdel);
  },
  wrap: function(jdel) {
    var obj = Object.create(HumanAgentService.prototype, {});
    HumanAgentService.apply(obj, arguments);
    return obj;
  },
  unwrap: function(obj) {
    return obj._jdel;
  }
};
HumanAgentService._create = function(jdel) {
  var obj = Object.create(HumanAgentService.prototype, {});
  HumanAgentService.apply(obj, arguments);
  return obj;
}
module.exports = HumanAgentService;