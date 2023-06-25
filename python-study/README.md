# 파이썬 설치

## Pyenv 설치

```
$ brew update
$ brew install pyenv
```

```
$ vi ~/.zshrc # 아래 내용 추가

if command -v pyenv 1>/dev/null 2>&1; then
  eval "$(pyenv init -)"
fi
PATH=$(pyenv root)/shims:$PATH
```

* vi ~/.zshrc # 아래 내용 추가

````
$ pyenv install 3.8.17 # 특정 버전 파이썬 설치
$ pyenv global 3.8.17 # 특정 버전 글로벌로 지정
$ pyenv local 3.8.17 # 특정 버전 로컬로 지정
```