#!/usr/bin/env bash

# Validate arguments
if [ $# -lt 1 ]; then
    >&2 echo "Usage: $0 <username/repo_name | full_url> ...\n"
    exit 1
fi

# Iterate over arguments
for url in "$@"; do

    # Generate GitHub repository URL
    url=`sed -e 's@^[A-Za-z0-9-][A-Za-z0-9-]*/[A-Za-z0-9-][A-Za-z0-9-]*$@https://github.com/&@' <<< "$url"`

    # Fetch README page
    echo "Fetching: $url"
    curl -sL "$url" \
    | sed -n '/<script type="application\/json" data-target="react-partial.embeddedData">/,/<\/script>/p' \
    | sed 's/\\u003e/>/g; s/\\u002F/\//g; s/\\u0022/"/g; s/\\//g' \
    | grep -oE 'https?://camo.githubusercontent.com/[^"]+' \
    | while read -r url; do

        # Purge resources
        echo "Purging: $url"
        curl -sX PURGE "$url" >/dev/null &

    done &
done

# Wait all jobs done
wait
