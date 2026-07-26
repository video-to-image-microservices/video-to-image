package video.to.image.worker_ms

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class WorkerMsApplication

fun main(args: Array<String>) {
	runApplication<WorkerMsApplication>(*args)
}
