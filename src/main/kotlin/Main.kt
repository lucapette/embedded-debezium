import io.debezium.engine.DebeziumEngine
import io.debezium.engine.format.Json
import io.debezium.engine.format.KeyValueChangeEventFormat
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.runBlocking
import sun.misc.Signal
import java.util.Properties
import java.util.concurrent.Executors



fun main(): Unit = runBlocking {
    Signal.handle(Signal("INT")) {
        this.coroutineContext.cancelChildren()
    }

    val props = Properties()
    props["name"] = "engine-test"

    props["connector.class"] = "io.debezium.connector.postgresql.PostgresConnector"
    props["offset.storage"] = "org.apache.kafka.connect.storage.FileOffsetBackingStore"
    props["offset.storage.file.filename"] = "/tmp/offsets.dat"
    props["offset.flush.interval.ms"] = "60000"

    props["database.hostname"] = "localhost"
    props["database.port"] = "5432"
    props["database.user"] = "lucapette"
    props["database.password"] = ""
    props["database.dbname"] = "debezium-test"
    props["topic.prefix"] = "test"
    props["plugin.name"] = "pgoutput"
    props["converter.schemas.enable"] = "false"
    props["table.include.list"] = "public.test"

    val engine = DebeziumEngine.create(KeyValueChangeEventFormat.of(Json::class.java, Json::class.java))
        .using(props)
        .notifying { record ->
            println(record)
        }.build()

    val executor = Executors.newSingleThreadExecutor()
    executor.execute(engine)

    Runtime.getRuntime().addShutdownHook(Thread {
        engine.close()
        executor.shutdown()
    })
}
