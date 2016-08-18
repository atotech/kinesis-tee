# Kinesis Tee

[ ![Build Status] [travis-image] ] [travis]
[ ![Release] [release-image] ] [releases]
[ ![License] [license-image] ] [license]

## Overview

Kinesis Tee is like [Unix tee] [tee], but for Kinesis streams. Use it to:

1. Transform the format of a Kinesis stream
2. Filter records from a Kinesis stream based on rules
3. Write a Kinesis stream to another Kinesis stream

Rules to apply to your Kinesis stream (e.g. for filtering) are written in JavaScript.

## How it works

You configure Kinesis Tee with a self-describing Avro configuration file containing:

1. A single **source stream** to read records from
2. A single **sink stream** to write records to
3. An optional **stream transformer** to convert the records to another supported format
4. An optional **steam filter** to determine whether to write the records to the sink stream

Here is an example:

```json
{
  "schema": "iglu:com.snowplowanalytics.kinesis-tee/Config/avro/1-0-0",
  "data": {
    "name": "My Kinesis Tee example",
    "sourceStream": {
      "name": "my-source-stream",
      "initialPosition": "TRIM_HORIZON", Or "LATEST"
      "maxRecords": 10000
    },
    "targetStream": {
      "name": "my-target-stream",
    },
    "transformer": "SNOWPLOW_TO_JSON", Or null
    "filter": {, Or null
      "javascript": "BASE64 ENCODED STRING"
    }
  }
}
```

Link to Avro schema: xxxx

### Transformers

The transformer `SNOWPLOW_TO_JSON` converts a Snowplow enriched event to a JSON using the Snowplow Scala Analytics SDK.

### Filters

Here is an example JavaScript filter function, where returning *true* means the record *will* be written to the target stream:

```javascript
function filter(record) {
	return (record.customer.status === "VIP");
}
```

This filter will only send a record from the Kinesis source stream to the sink stream if the record is parseable as JSON, contains a `customer` object which contains a `status` field, and that `status` field is set to the value "VIP". All other records will be silently discarded by Kinesis Tee.

## Roadmap

We have lots planned - pull requests welcome:

* Add support for streams in separate AWS regions ([#5] [5])
* Add support for streams in separate AWS accounts ([#1] [1])
* Add support for changing the partition key in sink streams ([#2] [2])
* Add support for source streams serialized in Avro, Protocol Buffers, Thrift ([#3] [3])
* Add support for writing rules in Java ([#4] [4])

## Copyright and license

Kinesis Tee is copyright 2015-2016 Snowplow Analytics Ltd.

Licensed under the [Apache License, Version 2.0] [license] (the "License");
you may not use this software except in compliance with the License.

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

[travis-image]: https://travis-ci.org/snowplow/kinesis-tee.png?branch=master
[travis]: http://travis-ci.org/snowplow/kinesis-tee

[release-image]: http://img.shields.io/badge/release-0.1.0-blue.svg?style=flat
[releases]: https://github.com/snowplow/kinesis-tee/releases

[license-image]: http://img.shields.io/badge/license-Apache--2-blue.svg?style=flat
[license]: http://www.apache.org/licenses/LICENSE-2.0

[tee]: https://en.wikipedia.org/wiki/Tee_%28command%29
[hocon]: https://github.com/typesafehub/config/blob/master/HOCON.md

[1]: https://github.com/snowplow/kinesis-tee/issues/1
[2]: https://github.com/snowplow/kinesis-tee/issues/2
[3]: https://github.com/snowplow/kinesis-tee/issues/3
[4]: https://github.com/snowplow/kinesis-tee/issues/4
[5]: https://github.com/snowplow/kinesis-tee/issues/5
