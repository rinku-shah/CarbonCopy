# Carbon-copy: Abstractions for Dataplane State Replication and Fault tolerance in Software-defined networks

Recent advancements of programmable dataplanes have caught the attention of the research community to offload application computations with high network I/O activity to the dataplane. This project intends to reduce the P4 programmer's effort by providing abstractions for replicating offloaded application state that resides on switches, and application agnostic fail-over handling.

We have implemented three state replication designs, and compared them with the traditional SDN based state replication design. The choice of the design depends on the application behavior and requirement. We name our designs as follows:

1. Traditional (basic)
2. CC-sync
3. CC-async
4. CC-CP
