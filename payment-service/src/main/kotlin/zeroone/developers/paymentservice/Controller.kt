package zeroone.developers.paymentservice
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import lombok.RequiredArgsConstructor
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.ExceptionHandler


@ControllerAdvice
class ExceptionHandler(private val errorMessageSource: ResourceBundleMessageSource) {

    @ExceptionHandler(BillingExceptionHandler::class)
    fun handleAccountException(exception: BillingExceptionHandler): ResponseEntity<BaseMessage> {
        return ResponseEntity.badRequest().body(exception.getErrorMessage(errorMessageSource))
    }
}

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
class PaymentController(val service: PaymentService) {


    @Operation(summary = "Get all payments", description = "Fetches all payments from the database.")
    @ApiResponses(ApiResponse(responseCode = "200", description = "Successfully fetched all payments"))
    @GetMapping
    fun getAll() = service.getAll()


    @Operation(summary = "Get all payments with pagination", description = "Fetches payments with pagination support.")
    @ApiResponses(ApiResponse(responseCode = "200", description = "Successfully fetched paginated payments"))
    @GetMapping("/page")
    fun getAll(
        @RequestParam(value = "page", defaultValue = "0") page: Int,
        @RequestParam(value = "size", defaultValue = "10") size: Int) =
        service.getAll(page, size)


    @Operation(summary = "Get payment by ID", description = "Fetches a single payment based on the provided ID.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successfully fetched the payment"),
        ApiResponse(responseCode = "404", description = "Payment not found"))
    @GetMapping("{id}")
    fun getOne(@PathVariable id: Long) = service.getOne(id)


    @Operation(summary = "Create new payment", description = "Creates a new payment record.")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Payment successfully created"),
        ApiResponse(responseCode = "400", description = "Invalid request data"))
    @PostMapping
    fun create(@RequestBody @Valid request: PaymentCreateRequest) = service.create(request)


    @Operation(summary = "Update existing payment", description = "Updates an existing payment based on the provided ID.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Payment successfully updated"),
        ApiResponse(responseCode = "404", description = "Payment not found"),
        ApiResponse(responseCode = "400", description = "Invalid request data"))
    @PutMapping("{id}")
    fun update(@PathVariable id: Long, @RequestBody @Valid request: PaymentUpdateRequest) = service.update(id, request)


    @Operation(summary = "Delete payment by ID", description = "Deletes a payment based on the provided ID.")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Payment successfully deleted"),
        ApiResponse(responseCode = "404", description = "Payment not found"))
    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)
}







