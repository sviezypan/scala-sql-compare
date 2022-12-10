package com.softwaremill.sql

import com.dimafeng.testcontainers.SingleContainer
import com.dimafeng.testcontainers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import zio._
import org.flywaydb.core.Flyway

object TestContainer {

  def postgres(
      imageName: String = "postgres:alpine"
  ): ZIO[Scope, Throwable, PostgreSQLContainer] =
    ZIO.acquireRelease {
      ZIO.attemptBlocking {
        val c = new PostgreSQLContainer(
          dockerImageNameOverride = Option(imageName).map(DockerImageName.parse)
        )
        c.start()
        val flyway = new Flyway()
        flyway.setDataSource(
          c.container.getJdbcUrl(),
          c.container.getUsername(),
          c.container.getPassword()
        )
        flyway.migrate()
        c
      }
    } { container =>
      ZIO.attemptBlocking(container.stop()).orDie
    }
}
