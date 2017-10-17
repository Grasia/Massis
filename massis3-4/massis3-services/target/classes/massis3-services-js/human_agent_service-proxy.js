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
!function (factory) {
  if (typeof require === 'function' && typeof module !== 'undefined') {
    factory();
  } else if (typeof define === 'function' && define.amd) {
    // AMD loader
    define('massis3-services-js/human_agent_service-proxy', [], factory);
  } else {
    // plain old include
    HumanAgentService = factory();
  }
}(function () {

  /**
 @class
  */
  var HumanAgentService = function(eb, address) {

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
     Creates a human in the specified location [x,y,z]

     @public
     @param location {Object} 
     @param resultHandler {function} 
     */
    this.createHuman = function(location, resultHandler) {
      var __args = arguments;
      if (__args.length === 2 && (typeof __args[0] === 'object' && __args[0] != null) && typeof __args[1] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"location":__args[0]}, {"action":"createHuman"}, function(err, result) { __args[1](err, result &&result.body); });
        return;
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"humanId":__args[0]}, {"action":"destroyHuman"}, function(err, result) { __args[1](err, result &&result.body); });
        return;
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"humanIds"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"name":__args[0]}, {"action":"createHumanInArea"}, function(err, result) { __args[1](err, result &&result.body); });
        return;
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"humanId":__args[0], "animationName":__args[1], "loop":__args[2]}, {"action":"animate"}, function(err, result) { __args[3](err, result &&result.body); });
        return;
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"humanId":__args[0]}, {"action":"getLocation"}, function(err, result) { __args[1](err, result &&result.body); });
        return;
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"humanId":__args[0]}, {"action":"isFollowingPath"}, function(err, result) { __args[1](err, result &&result.body); });
        return;
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"humanId":__args[0], "target":__args[1]}, {"action":"moveTowards"}, function(err, result) { __args[2](err, result &&result.body); });
        return;
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"humanId":__args[0]}, {"action":"stopMoving"}, function(err, result) { __args[1](err, result &&result.body); });
        return;
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"humanId":__args[0], "range":__args[1]}, {"action":"getHumanIdsInRange"}, function(err, result) { __args[2](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param result {function} 
     */
    this.positionStreamingAddress = function(result) {
      var __args = arguments;
      if (__args.length === 1 && typeof __args[0] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {}, {"action":"positionStreamingAddress"}, function(err, result) { __args[0](err, result &&result.body); });
        return;
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"humanId":__args[0]}, {"action":"humanExists"}, function(err, result) { __args[1](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

  };

  if (typeof exports !== 'undefined') {
    if (typeof module !== 'undefined' && module.exports) {
      exports = module.exports = HumanAgentService;
    } else {
      exports.HumanAgentService = HumanAgentService;
    }
  } else {
    return HumanAgentService;
  }
});