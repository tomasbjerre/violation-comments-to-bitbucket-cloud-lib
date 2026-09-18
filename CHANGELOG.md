## 2.2.0 (2026-09-18)

### Features

-  add option to attach a task to each posted comment ([d23b3](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/d23b379365e2296) Tomas Bjerre)  
-  resolve comments instead of removing them ([41b3a](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/41b3a61f588e9df) Tomas Bjerre)  
-  migrate to Jackson 3 and tolerate unknown diffstat status values ([585b5](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/585b564b5ca71da) Tomas Bjerre)  

### Bug Fixes

-  drop the task-comment mixin workaround, upgrade to 3.2.4 ([794de](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/794de1b9a9ac6c6) Tomas Bjerre)  
-  fall back to deleting a comment when it can't be resolved ([5dc29](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/5dc299b59a17f6b) Tomas Bjerre)  
-  satisfy static code analysis in JacksonJakartaJsonProvider ([3530b](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/3530b79fb94dcc2) Tomas Bjerre)  
-  avoid RC dependency and resolutionStrategy force ([4b81a](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/4b81a28bb9b5cc8) Tomas Bjerre)  

### Dependency updates

- update dependency se.bjurr.violations:violation-comments-lib to v1.111.0 (#10) ([94e37](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/94e37cbfa28d35b) renovate[bot])  
- update plugin se.bjurr.gradle.bundle-jar to v2.3.0 (#8) ([095e5](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/095e53c5f47d22c) renovate[bot])  
### Other changes

**Merge pull request #13 from tomasbjerre/feat/create-comment-tasks**

* feat: add option to attach a task to each posted comment 

[ad2a0](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/ad2a0e9cdb4057a) Tomas Bjerre *2026-09-18 07:59:20*

**Merge pull request #12 from tomasbjerre/test/real-pr-wiremock-integration-tests**

* test: add integration tests replaying real Bitbucket Cloud PR traffic 

[5f35f](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/5f35fe36f008308) Tomas Bjerre *2026-09-18 05:20:28*

**Merge pull request #7 from tomasbjerre/feature/jackson3-migration**

* feat: migrate to Jackson 3 and tolerate unknown diffstat status values 

[d31a3](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/d31a31c31686877) Tomas Bjerre *2026-09-16 18:57:42*


## 2.1.0 (2026-09-14)

### Features

-  **ci**  add draft/publish release workflow ([d7dcf](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/d7dcf21f7de3af9) Tomas Bjerre)  

### Dependency updates

- update gradle wrapper to 9.7.1 ([ce200](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/ce2003a3fb963e5) Tomas Bjerre)  
### Other changes

**Superseded by centralized publish-draft-releases.yaml in .github**


[adfc2](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/adfc27b6df27211) Tomas Bjerre *2026-09-14 19:58:50*

**Auto-publish pending draft releases weekly**


[cb8ca](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/cb8ca27f9242680) Tomas Bjerre *2026-09-14 19:47:48*


## 2.0.5 (2026-09-14)

### Other changes

**Fix broken Maven Central badge in README**

* maven-badges.herokuapp.com is down; switch to img.shields.io badge 
* linking to search.maven.org. 
* Co-Authored-By: Claude Sonnet 5 &lt;noreply@anthropic.com&gt; 
* Claude-Session: https://claude.ai/code/session_011PGVf85V1VhJj4oM3rKh7b 

[fb24e](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/fb24e46d3d3a5fc) Tomas Bjerre *2026-09-13 08:52:01*


## 2.0.4 (2025-11-30)

### Bug Fixes

-  Cannot parse JSON responses since 1.35.x ([a8407](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/a84077b33b94903) Tomas Bjerre)  

### Dependency updates

- gradle 9 ([eb794](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/eb7945eb6951f9d) Tomas Bjerre)  
## 2.0.3 (2025-07-17)

### Bug Fixes

-  missing MessageBodyReader ([c54fb](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/c54fbaadb8f2025) Tomas Bjerre)  

## 2.0.2 (2025-07-17)

### Bug Fixes

-  missing MessageBodyReader ([2decd](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/2decd6f99873dcf) Tomas Bjerre)  

## 2.0.1 (2025-07-17)

### Bug Fixes

-  missing MessageBodyReader ([475ac](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/475ac4bfaace230) Tomas Bjerre)  

## 2.0.0 (2025-07-17)

### Breaking changes

-  migrating to jakarta namespace ([be220](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/be220c6802030e5) Tomas Bjerre)  

## 1.17.2 (2025-07-17)

### Bug Fixes

-  trying to use new Sonatype OSS API ([9ec6a](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/9ec6a1562cb0eb5) Tomas Bjerre)  


# violation-comments-to-bitbucket-cloud-lib changelog

Changelog of violation-comments-to-bitbucket-cloud-lib.

## 1.16.1
### No issue

**new build script**


[00a576f5ff513d3](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/00a576f5ff513d3) Tomas Bjerre *2021-04-04 15:11:32*


## 1.15
### No issue

**New: custom logging in violations-lib**


[8c6a3836529c105](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/8c6a3836529c105) Tomas Bjerre *2020-07-05 12:47:57*


## 1.14
### No issue

**Fix destination line for comment**


[1ade8a9ca2d36ee](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/1ade8a9ca2d36ee) michal-luszczuk *2020-02-16 16:49:38*


## 1.13
### No issue

**Switched from using username to workspace while removing comment**


[1b5ba6618f2aea5](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/1b5ba6618f2aea5) michal-pc *2020-02-16 16:16:15*


## 1.12
### GitHub [#1](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/issues/1) Delete Comments Endpoint passes Username in the place of Workspace  

**Changing username to workspace for repositories**

* tomasbjerre/violation-comments-to-bitbucket-cloud-lib 

[0bb3a5bdaf5d9a2](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/0bb3a5bdaf5d9a2) Tomas Bjerre *2020-02-02 19:10:14*

**Changing username to workspace for repositories**

* tomasbjerre/violation-comments-to-bitbucket-cloud-lib 

[bb8277e72183021](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/bb8277e72183021) Tomas Bjerre *2020-02-02 18:02:27*


### No issue

**doc**


[56f72b3c2fc0ff9](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/56f72b3c2fc0ff9) Tomas Bjerre *2019-10-09 17:10:11*

**Create FUNDING.yml**


[ca9ee2bee584efd](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/ca9ee2bee584efd) Tomas Bjerre *2019-09-28 07:04:27*


## 1.11
### No issue

**junit**


[e59e8a992f9ae09](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/e59e8a992f9ae09) Tomas Bjerre *2019-06-16 16:20:42*


## 1.9
### No issue

**commentOnlyChangedFiles**


[3ffc40190212ee1](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/3ffc40190212ee1) Tomas Bjerre *2019-06-16 07:47:34*


## 1.8
### No issue

**MessageBodyReader for text/plain**


[00eb55b18b4c900](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/00eb55b18b4c900) Tomas Bjerre *2019-06-11 21:00:51*


## 1.7
### No issue

**Version from Maven Central**


[07a322bde604eeb](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/07a322bde604eeb) Tomas Bjerre *2019-06-11 17:31:48*

**DefaultTextPlain**


[91d3d214420a2bf](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/91d3d214420a2bf) Tomas Bjerre *2019-06-09 20:15:11*

**Test Handling test/plain**


[7b898b031950be7](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/7b898b031950be7) Tomas Bjerre *2019-06-09 20:06:08*

**Handling test/plain**


[6e6f87dc506b611](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/6e6f87dc506b611) Tomas Bjerre *2019-06-09 19:38:03*

**shouldCommentOnlyChangedContent**


[32a73105c94eba9](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/32a73105c94eba9) Tomas Bjerre *2019-06-09 19:17:42*

**Avoid NPE**


[b5d7944f0dac3d1](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/b5d7944f0dac3d1) Tomas Bjerre *2019-06-09 16:57:28*

**Adjustments**


[d649b15d2624d79](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/d649b15d2624d79) Tomas Bjerre *2019-06-09 16:28:19*

**Adjustments**


[41bd3d4ba4536e2](https://github.com/tomasbjerre/violation-comments-to-bitbucket-cloud-lib/commit/41bd3d4ba4536e2) Tomas Bjerre *2019-06-09 16:22:23*


