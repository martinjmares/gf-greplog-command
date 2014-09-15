if [ -z $1 ]; then
	CMD="GET"
else
	CMD=$1
fi
export http_proxy=""
curl -X $CMD "http://gf-cmd-expr.martin-jacek-mares.cloudbees.net/rest/emails?passwd=hjnal3456neejaltt7"