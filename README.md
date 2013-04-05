## Former home of the ThreeTen/JSR-310 project

JSR-310 is a project under the Java Community Process to provide
a modern date and time library for the JDK. ThreeTen is the name
of the Reference Implementation used to develop the specification.

### Status

This GitHub project is currently dormant, as active development
of JSR-310 has moved to OpenJDK to integrate with JDK1.8:
* Project: http://openjdk.java.net/projects/threeten/
* Code: http://hg.openjdk.java.net/threeten/threeten/jdk
* Mailing list: http://mail.openjdk.java.net/mailman/listinfo/threeten-dev

The active issue tracker is still located here at GitHub:
* Issues: https://github.com/ThreeTen/threeten/issues

A backport of the API, but not the JSR, has been provided for JDK1.7 users:
 https://github.com/ThreeTen/threetenbp
This is available in the Maven Central repository and uses the ThreeTen name.

A helpful home page has been created, where some documentation is being
developed (applicable to JSR-310 and the ThreeTen backport).
 http://threeten.github.com/


### History

As a long running project, the project has moved location down the years.

#### Source code history

This GitHub project was the home of the source code for a period from 2011-06-24 to 2012-12-04.

The initial commit to OpenJDK occurred on 2012-11-09:
 http://hg.openjdk.java.net/threeten/threeten/jdk/rev/b74a5a99159a
using commit from GitHub:
 https://github.com/ThreeTen/threeten/commits/b9566e443b6279f7f1abe675ff012575fb3018f3
This commit was made by scolebourne to establish IP transfer to OpenJDK.

Commits then occurred on both GitHub (scolebourne) and OpenJDK (rriggs, sherman)
for the period from 2012-11-09 to 2012-12-04, a total of 136 GitHub commits.
Here is an example of a commit (the first one) that was ported manually:
 https://github.com/ThreeTen/threeten/commit/1eb175e448fd77fdd1971c7d3266199aa4bba89c
 http://hg.openjdk.java.net/threeten/threeten/jdk/rev/4692637fbb48
Note that the porting changes the author (from scolebourne to rriggs).

The last commit to this repository that was ported to OpenJDK was on 2012-12-04:
 https://github.com/ThreeTen/threeten/commit/280f25a00d96df7943b89265ece46e593b823926
 http://hg.openjdk.java.net/threeten/threeten/jdk/rev/4cc0f3c099e0


Prior to being hosted at GitHub, the source code was hosted at Sourceforge.
 http://threeten.sourceforge.net/
 http://sourceforge.net/projects/threeten/
The VCS used was git from 2011-06-24 to 2011-06-24 (part of migrating to GitHub):
 http://sourceforge.net/p/threeten/code
The VCS was svn up from 2010-12-24 to 2011-06-10:
 http://sourceforge.net/p/threeten/svn/1497/tree/
Last commit at Sourceforge became this commit at GitHub:
 https://github.com/ThreeTen/threeten/commit/83f2a944dc8f0ab4fb240132977f56958aede9be


Prior to being hosted as Sourceforge, the source code was hosted at Java.net.
 http://java.net/projects/jsr-310
The VCS was svn:
 http://java.net/projects/jsr-310/sources/svn/show
Last commit at Java.net:
 http://java.net/projects/jsr-310/sources/svn/revision/1336
became this commit at Sourceforge:
 http://sourceforge.net/p/threeten/svn/1281/tree/

No source code commit history was lost during the move from Java.net to Sourceforge to GitHub.
All source code commit history was lost in the move to OpenJDK.


#### Mailing list history

Currently at OpenJDK:
 http://mail.openjdk.java.net/mailman/listinfo/threeten-dev

Previously at Sourceforge:
 http://sourceforge.net/mailarchive/forum.php?forum_name=threeten-develop

Prior to that at Java.net
 http://java.net/projects/jsr-310/lists/dev/archive


#### Summary
| Location    | VCS | Dates                         |
| ----------- | --- | ----------------------------- |
| Java.net    | svn | from inception  to 2010-12-24 |
| Sourceforge | svn | from 2010-12-24 to 2011-06-24 |
| Sourceforge | git | from 2011-06-24 to 2011-06-24 |
| GitHub      | git | from 2011-06-24 to 2012-12-04 |
| OpenJDK     | hg  | from 2012-12-04 onwards       |

