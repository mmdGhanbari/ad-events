package ir.tapsell.adevents.repository

import ir.tapsell.adevents.entity.AdEvent
import org.springframework.data.cassandra.repository.CassandraRepository
import org.springframework.stereotype.Repository

@Repository
interface AdEventRepository: CassandraRepository<AdEvent, String> {}