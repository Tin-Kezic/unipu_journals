package hr.unipu.journals.domain.valueobject

@JvmInline
value class Email(val value: String) {
    init {
        require("@" in value) { "Invalid email" }
        TODO("make a require which checks that mail isn't already registered")
        //require() { } https://en.wikipedia.org/wiki/Email_address #syntax
    }
}