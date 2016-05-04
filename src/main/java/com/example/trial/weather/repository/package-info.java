/**
 * This package contains the repository implementations.
 *
 * Instead of using one predefined repository for all the metadata (for example
 * static variables) we expanded to allow the possibility of use multiple ones.
 *
 * In the future may be persistence is required (as suggested in the comments)
 * or remote/distributed/fault tolerant implementations are required (Redis,
 * Memcache, MongoDB, etc.), having this separation of concerns in the service
 * the change becomes almost trivial/transparent.
 */
package com.example.trial.weather.repository;
