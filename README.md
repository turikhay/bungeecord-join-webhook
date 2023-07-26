# BungeeСord webhook client

<p>
  <a href="https://github.com/turikhay/bungeecord-join-webhook/blob/main/LICENSE.txt">
    <img src="https://img.shields.io/github/license/turikhay/bungeecord-join-webhook">
  </a>
  <a href="https://github.com/turikhay/bungeecord-join-webhook/actions/workflows/nightly.yml">
    <img src="https://github.com/turikhay/bungeecord-join-webhook/actions/workflows/nightly.yml/badge.svg" />
  </a>
  <a href="https://www.buymeacoffee.com/turikhay">
    <img src="https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png" height="20px">
  </a>
</p>

Sends HTTP requests to the specified destination when players join or leave the network.

```yaml
# config.yml
webhookUrl: https://example.com/
```

Typical requests look like this:

```shell
$ curl -X POST -d "event=join&username=Notch&address=104.86.46.60:1337" https://example.com
$ curl -X POST -d "event=quit&username=Notch&address=104.86.46.60:1337" https://example.com
```

<table width=100%>
    <thead>
        <tr>
            <th>Event</th>
            <th>Parameters</th>
            <th>Description</th>
            <th>Examples</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td rowspan=2>(any)</td>
            <td><code>event</code></td>
            <td>Name of the event</td>
            <td><code>join</code>, <code>quit</code></td>
        </tr>
        <tr>
            <td><code>timestamp</code></td>
            <td>Timestamp in ISO 8601 format (UTC)</td>
            <td><code>2023‐07‐26T13:12:25Z</code></td>
        </tr>
        <tr>
            <td rowspan=2>
                <code>join</code> /
                <br/>
                <code>quit</code>
            </td>
            <td><code>username</code></td>
            <td>Username</td>
            <td><code>Notch</code>, <code>turikhay</code></td>
        </tr>
        <tr>
            <td><code>address</code></td>
            <td>User address</td>
            <td><code>23.192.55.22:1337</code></td>
        </tr>
    </tbody>
</table>
