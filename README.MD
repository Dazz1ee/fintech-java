## Опыт и навыки
Опыта работы нет. Делал несколько проектов, чтобы хоть как-то улучшить навыки. Так в последнем проекте использовал Spring Boot, Spring JDBC (решил не использовать Spring Data, т.к. решил попробовать что-то новое) и впервые поработал с Kafka, логированием,  JUnit, Testcontainers и Mockito (но это было на базом уровне). Про git, docker и k8s немного рассказывали в университете.

## Цели
Хотелось бы улучшить уже существующие навыки и приобрести новые, чтобы по окончанию курса устроится на работу и ориентироваться в технологиях, которые используются в команде.

## Курсовой проект
Пока ещё не определился.

## Команды git
- **init** - инициализация нового локального репозитория (создает папку ".git"). Можно использовать в связке с ```git remote```
  Пример:
```bash
git status
fatal: not a git repository (or any of the parent directories): .git
git init
Initialized empty Git repository in ...
git status
On branch master

No commits yet

nothing to commit (create/copy files and use "git add" to track)
```
- **clone** - cоздает копию удаленного репозитория, при этом не требуется использовать перед этим команду ```git init```.
  Пример:
```bash
PS C:\...\Новая папка> git clone https://github.com/Dazz1ee/fintech-java.git
Cloning into 'fintech-java'...
remote: Enumerating objects: 2, done.
remote: Counting objects: 100% (2/2), done.
remote: Total 2 (delta 0), reused 2 (delta 0), pack-reused 0
Receiving objects: 100% (2/2), done.
PS C:\...\Новая папка> dir


    Каталог: C:\...\Новая папка

Mode                 LastWriteTime         Length Name
----                 -------------         ------ ----
d-----        12.09.2023     16:39                fintech-java

PS C:\...\Новая папка> git clone https://github.com/Dazz1ee/fintech-java.git directory
Cloning into 'directory'...
remote: Enumerating objects: 2, done.
remote: Counting objects: 100% (2/2), done.
remote: Total 2 (delta 0), reused 2 (delta 0), pack-reused 0
Receiving objects: 100% (2/2), done.
PS C:\...\Новая папка> dir


    Каталог: C:\...\Новая папка


Mode                 LastWriteTime         Length Name
----                 -------------         ------ ----
d-----        12.09.2023     16:42                directory
d-----        12.09.2023     16:39                fintech-java
```
- **add** - добавляет под контроль версий выбранный файл(ы).
  Пример:
```bash
PS C:\...\fintech-java> git add test1.txt
PS C:\...\fintech-java> git status
Untracked files:
  (use "git add <file>..." to include in what will be committed)
        test2.txt
        test3.txt

PS C:\...\fintech-java> git add .
PS C:\...\fintech-java> git status

Changes to be committed:
  (use "git restore --staged <file>..." to unstage)
        new file:   test1.txt
        new file:   test2.txt
        new file:   test3.txt

```
- **commit** - фиксирует изменения, делает коммит в локальном репозитории.
  Пример:
```bash
PS C:\...\fintech-java> git commit -m "test"
[master e558dd4] test
 3 files changed, 0 insertions(+), 0 deletions(-)
 create mode 100644 test1.txt
 create mode 100644 test2.txt
 create mode 100644 test3.txt
```
e558dd4 - часть хеша нового коммита
- **push** - отправляет изменения в удаленный репозиторий. Можно отправлять изменения как одной ветки, так и всех, также можно отправить изменения даже на другую ветку
  Пример:
```bash
PS C:\...\Новая папка> git push origin develop
    ...
 * [new branch]      develop -> develop
PS C:\...\Новая папка> git push origin develop:master
    ...
 * [new branch]      develop -> master
```
- **pull** - Извлечение данных из удаленной ветки и слияние. Если используется в чистом в виде, то сначала скачиваются изменения из удаленного репозитория, а потом используется merge, если использовать "--rebase", то merge заменится на rebase
  Пример:
```bash
PS C:\...\test> git pull origin develop
remote: Enumerating objects: 3, done.
remote: Counting objects: 100% (3/3), done.
remote: Compressing objects: 100% (2/2), done.
remote: Total 2 (delta 0), reused 2 (delta 0), pack-reused 0
Unpacking objects: 100% (2/2), 217 bytes | 43.00 KiB/s, done.
From https://github.com/Dazz1ee/test
 * branch            develop    -> FETCH_HEAD
   5c50172..58813a9  develop    -> origin/develop
Merge made by the 'ort' strategy.
 test2.txt | 0
 1 file changed, 0 insertions(+), 0 deletions(-)
 create mode 100644 test2.txt

```