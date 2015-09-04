# Kinesis Tee

[ ![Build Status] [travis-image] ] [travis]
[ ![Release] [release-image] ] [releases]
[ ![License] [license-image] ] [license]

## Overview

Kinesis Tee is like [Unix tee] [tee], but for Kinesis streams. Use it to:

1. Mirror a Kinesis stream to another Kinesis stream
2. Filter records from a Kinesis stream based on rules
3. Fork a Kinesis stream into multiple Kinesis streams based on rules

Rules to apply to your Kinesis stream are written in JavaScript.

## How it works

You configure Kinesis Tee with a [HOCON-format] [hocon] configuration file containing:

1. A single **source stream** to read from
2. One or more **sink streams** to write events to
3. **JavaScript rules** for each source stream record to determine which sink stream(s) to write to

Define the source stream like so:

```
in {
	stream-name: "my-source-stream"
	initial-position: "TRIM_HORIZON" # Or LATEST   
	max-records: 10000
}
```

Define sink streams like this:

```
out {
	a {
		stream-name: "internal-audit-stream"
	}
	b {
		stream-name: "half-of-records-stream"
	}
	c {
		stream-name: "other-half-of-records-stream"
	}
}
```

Then add configure routings from your source stream to your sink streams like this:

```
routes {
	static: [ "a" ]
	javascript {
		inline: """
			function route(record) {
				var sink = Math.floor(Math.random() * 2) ? 'b' : 'c';
				return [ sink ];
			}"""
	}
}
```

This configuration will:

1. Send every Kinesis record in the source stream to sink stream `a`
2. Send every Kinesis record to either sink stream `b` or `c` (but never both or neither) at random

## Roadmap

We have lots planned - pull requests welcome:

* Add support for streams in separate AWS regions ([#5] [5])
* Add support for streams in separate AWS accounts ([#1] [1])
* Add support for changing the partition key in sink streams ([#2] [2])
* Add support for source streams serialized in Avro, Protocol Buffers, Thrift ([#3] [3])
* Add support for writing rules in Java ([#4] [4])

## Copyright and license

Kinesis Tee is copyright 2015 Snowplow Analytics Ltd.

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
