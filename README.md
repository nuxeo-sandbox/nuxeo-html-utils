# nuxeo-html-utils

This plug-in contains utilities to parse an HTML blob.

Internally, it uses the _Jericho HTML Parser_ library (no need to install anything in your server, this library is already used by Nuxeo)

## Operations

* **`HTML: Get Links`** (id `HTML.GetLinks`)
  * Accepts a `Blob`, `Document` or `string`, returns a `String`
  * Parses the html for every tag with a "src" or a "href" attribute, and returns a JSON string of an array of objects. Each object has a `tag`, `attribute`, `text` and `link` field.
  * When the input is `Document`, you can use the `xpath` parameter to let the plug-in know where to get the blob from (default is `file:content`).
  * Notice: If the input is `Document` and `xpath` is left empty or there is no blob, the plug-in will check if the document has the `note` schema. If yes, it uses its `note:note` field for parsing
  * Here is an example  using JavaScript automation:

```
// In this example, the JavaScript receives a Document as input, and uses its file:content field to get the blob.
function run(input, params) {
  var blob, resultStr, resultJson;
  
  blob = doc["file:content"];
  resultStr = HTML.GetLinks(blog, {});
  // We have the JSON string, convert it to full JSON
  resultJson = JSON.parse(resultStr);
  // Now, we can loop and get the values of each field:
  resultJson.forEach(function(obj) {
    obj.tag contains "a" or "img" for example. Could contains "script", "link", ...
    obj.attribute contains "href" or "src"
    obj.text  contains "hello" for <a href="http://site.com">Hello</a>
    obj.link  contains "http://site.com" for <a href="http://site.com">Hello</a>
  }
}
```

* **`HTML: Get PLain Text`** (id `HTML.GetPLainText`)
  * Accepts a `Blob`, returns a `String`
  * Parses the html and returns the plain text content.
  * Parameters:
    * `includeHyperlinkURLs`: Boolean, optionnal. Default value is `false`.
    * `includeAlternateText`: Boolean, optionnal. Default value is `false`.
    * `convertNonBreakingSpaces`: Boolean, optionnal. Default value is `false`.
    * `lineSeparator`: String, optionnal. Default value is `LF` (char #10, `"\n"`)
    * `xpath`: Thge xpath to use when the input is `Document`. Default value is `file:contet`
      * Notice: If the input is `Document` and `xpath` is left empty or there is no blob, the plug-in will check if the document has the `note` schema. If yes, it uses its `note:note` field for parsing


## Build

    cd /path/to/nuxeo-html-utils
    mvn clean install


## License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## About Nuxeo

Nuxeo provides a modular, extensible Java-based [open source software platform for enterprise content management](http://www.nuxeo.com) and packaged applications for Document Management, Digital Asset Management and Case Management. Designed by developers for developers, the Nuxeo platform offers a modern architecture, a powerful plug-in model and extensive packaging capabilities for building content applications.
