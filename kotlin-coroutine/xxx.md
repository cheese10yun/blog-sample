                  xxxSearchRequests
                .asFlow()
                .flatMapMerge { xxxSearchRequest ->
                    flow {
                        val aggregationKeys = mutableListOf<AggregationKey>()
                        xxxClient
                            .getXXX(xxxSearchRequest)
                            .onFailure {
                                log.error("xxxSearchRequest: $xxxSearchRequest 조회 실패, Error Response: ${it.message}")
                            }
                            .onSuccess { xxxResponse ->
                                for (xxxx in xxxResponse) {
                                    aggregationKeys.add(
                                        AggregationKey(
                                            cid = xxxx.cid,
                                            xxxxId = xxxx.xxxxId,
                                            channelType = xxxx.channelType
                                        )
                                    )
                                }
                            }
                        emit(aggregationKeys)
                    }
                }
                .toList()
                .flatten()