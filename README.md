# Transaction Log With Currency Conversion

### Requirements

See [WEX TAG and Gateways Product Brief.pdf](docs%2FWEX%20TAG%20and%20Gateways%20Product%20Brief.pdf)

### Usage

```shell
mvn verify
cd ./target
java --enable-preview -jar ./app.jar
```

### Notes

* App deliberately allows storing transactions with both positive and negative amounts 
* App deliberately truncates long description instead of erroring to match amount rounding approach
* App uses a mix of checked and unchecked exceptions leaning towards unchecked, but it could be tuned if required
* Exchange service implementation uses hardcoded API url since we do not have configuration file, and the implementation details are locked to that API
* Javadocs build is enabled, but only `main` method has a brief documentation to save time, ideally all public artifacts should be documented
