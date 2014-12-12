from fabric.api import local, put, cd, run, env
import daemon

env.use_ssh_config = True
env.app_name = 'spark_cluster_sample'
env.package_name = "%(app_name)s-1.0-SNAPSHOT" % env

def staging():
    env.hosts = [
        "spark-master-manually.ap-northeast-1"
    ]
    env.conffile = "staging.conf"
    env.httpport = "9000"
    env.mem = "2048"

def server_info():
    run('whoami')
    run('uname -a')

def deploy():
    local('play dist')

    put("target/universal/%(package_name)s.zip" % env, '~/.')

    stop()

    run("rm -rf %(package_name)s" % env)

    run("unzip %(package_name)s.zip" % env)

    run("rm %(package_name)s.zip" % env)

    run("ln -s ~/resources %(package_name)s/resources" % env)

    start()

def restart():
    stop()
    start()

def stop():
    with cd(env.package_name):
        if run('test -f RUNNING_PID', warn_only=True).succeeded:
            run('kill -SIGTERM `cat RUNNING_PID`')

def start():
    with cd(env.package_name):
        run("bin/%(app_name)s -DapplyEvolutions.default=true -Dconfig.file=conf/%(conffile)s -Dhttp.port=%(httpport)s -mem %(mem)s" % env, pty=False)

