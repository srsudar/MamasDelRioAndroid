# Mamás del Río

> The Android component of Mamás del Río


## Overview

Mamás del Río is a project to provide support to mothers living in rural
villages in the Peruvian Amazon. One component of the project is a way for
community agents to communicate with doctors and global health researchers in
Lima. This is the code for the Android app that facilitates communication
between the villagers and the researchers.

![](./playAssets/192.png "The Mamás del Río icon")

It is essentially a repackaging of [ODK
Collect](https://github.com/opendatakit/collect). The major change is that
sending an instance does not use an XForms server (e.g. ODK Aggregate).
Instead, the instance is converted to a JSON map and sent via a Whatsapp
message. These messages can then be sent via email and parsed to a CSV using
the [Mamás del Río Parser](https://srsudar.github.io/MamasDelRioParser/).


## Other Differences

There are a few other differences from standard Collect. Instance values with
names beginning with `ign_` will not be present in the final message. This
makes it possible to include values in the form that will not be snet via
Whatsapp.

A special instance value with the name `ign_messageTemplate`, if present, will
be used as the message below the JSON map in the Whatsapp message. E.g. the if
the `ign_messageTemplate` value is `I have info`, below the JSON map in the
message the string `I have info` will appear. This allows you to control what
human-readable message is sent. The default is `I have information`.

The value in `ign_messageTemplate` is also interpolated with other values in
the form, allowing you to customize it even further. E.g. consider a form with
a value named `age`, and the value in `ign_messageTemplate` with `I am ${age}
years old`. When that form is completed with the age value of `21`, the final
message will be `I am 21 years old`.

Using the guidelines in [this
thread](https://groups.google.com/forum/#!topic/opendatakit/Dfa4XAjlwrA), is it
possible to ensure the `ign_messageTemplate` is saved to the instance file
without being shown to the user (though I've not yet tried this myself).

