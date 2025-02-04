package com.example.primarydetail.ui

import com.example.primarydetail.model.Post
import com.example.primarydetail.services.ApiService
import com.example.primarydetail.services.PostsDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PostRepository @Inject constructor(
    private val client: ApiService,
    private val postsDao: PostsDao,
    private val ioDispatcher: CoroutineDispatcher
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getPosts(): Flow<List<Post>> {
        return postsDao.getAllPosts().flatMapLatest { posts ->
            if (posts.isEmpty()) {
                flow {
                    emit(getServerPosts())
                }.flatMapLatest {
                    postsDao.getAllPosts()
                }
            } else {
                flowOf(posts)
            }
        }
    }

    /**
     * If there is nothing in the database, get all posts and save them, this is just for example
     */
    suspend fun getServerPosts() = withContext(ioDispatcher) {
        insertPosts(client.getAllPosts())
    }

    /**
     * Calls the DAO to insert posts into the database
     * @param posts A list of posts to insert
     */
    private suspend fun insertPosts(posts: List<Post>) = postsDao.insertPosts(posts)

    /**
     * Calls the DAO to get a single post from the database
     * @return A [Post]
     */
    fun postById(postId: Long): Flow<Post> {
        return postsDao.postById(postId)
    }

    /**
     * Calls the DAO to update posts and mark them as read
     * @param postIds A list of IDs to update
     */
    suspend fun markRead(postIds: List<Long>) = postsDao.markRead(postIds)

    /**
     * Calls the DAO to update a post and mark it as read
     * @param postId The ID to update
     */
    suspend fun markRead(postId: Long) = postsDao.markRead(postId)

    /**
     * Calls the DAO to delete posts
     * @param postIds A list of IDs to delete
     */
    suspend fun deletePosts(postIds: List<Long>) = postsDao.deletePosts(postIds)

    /**
     * Calls the DAO to delete a post
     * @param postId The ID of the post to delete
     */
    suspend fun deletePost(postId: Long) = postsDao.deletePost(postId)
}
