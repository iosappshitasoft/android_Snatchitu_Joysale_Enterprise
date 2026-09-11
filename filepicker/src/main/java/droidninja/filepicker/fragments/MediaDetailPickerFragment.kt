package droidninja.filepicker.fragments

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import droidninja.filepicker.FilePickerConst
import droidninja.filepicker.PickerManager
import droidninja.filepicker.R
import droidninja.filepicker.adapters.FileAdapterListener
import droidninja.filepicker.adapters.PhotoGridAdapter
import droidninja.filepicker.models.Media
import droidninja.filepicker.utils.AndroidLifecycleUtils
import droidninja.filepicker.utils.ImageCaptureManager
import droidninja.filepicker.viewmodels.VMMediaPicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException


class MediaDetailPickerFragment : BaseFragment(), FileAdapterListener {
    lateinit var recyclerView: RecyclerView

    lateinit var emptyView: TextView
    lateinit var viewModel: VMMediaPicker

    private var mListener: PhotoPickerFragmentListener? = null
    private var photoGridAdapter: PhotoGridAdapter? = null
    private var imageCaptureManager: ImageCaptureManager? = null
    private lateinit var mGlideRequestManager: RequestManager
    private var fileType: Int = 0
    private var imageFileSize: Int = FilePickerConst.DEFAULT_FILE_SIZE
    private var videoFileSize: Int = FilePickerConst.DEFAULT_FILE_SIZE
    private var selectAllItem: MenuItem? = null
    private var mContext: Context? = null
    lateinit var manageText : TextView
    lateinit var manageFrameLayout : FrameLayout

    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Array<String>>

    var FilePathUri: Uri? = null
    var Image_Request_Code: Int = 7
    private var imagePickerLauncher: ActivityResultLauncher<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photo_picker, container, false)


    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        if (context is PhotoPickerFragmentListener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement PhotoPickerFragmentListener")
        }

        requestPermissionsLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result: Map<String, Boolean> ->
            var allGranted = true
            val grantedPermissions = StringBuilder("Granted permissions: ")

            for ((key, value) in result) {
                if (value) {
                    grantedPermissions.append(key).append(" ")
                } else {
                    allGranted = false
                }
            }
            Log.d("YourActivity", grantedPermissions.toString())

            if (allGranted) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED), 100)
                }
                else{
                    Log.d(TAG, "onAttach: permission granted")
                }
//                imagePickerLauncher?.launch("image/*");
                // All permissions granted
                // Perform your desired action here
            } else {
                var isOpen = false
                for ((key, value) in result) {
                    if (!value) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(Activity(), key)) {
                            // Request permissions again
                            requestPermissionsLauncher.launch(result.keys.toTypedArray())
                        } else {
                            Toast.makeText(
                                mContext,
                                "Permission is required to access more photos",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        break
                    }
                }
            }
        }

        imagePickerLauncher = registerForActivityResult<String, Uri>(
            ActivityResultContracts.GetContent()
        ) { result: Uri? ->
            if (result != null) {
                // Handle the selected image
                FilePathUri = result
                Log.d(TAG, "onAttach: "+result)

                try {


                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onItemSelected() {
        mListener?.onItemSelected()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(PickerManager.hasSelectAll())
        mGlideRequestManager = Glide.with(this)
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        ).get(VMMediaPicker::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        setupManageText()
    }
    private fun setupManageText() {
        val appName = "Joysale"
        val manageTextStr = "$appName has permission to access only a select number of photos. Manage"
        val underlineColor = ContextCompat.getColor(requireContext(), R.color.underlinedtxtcolor) // Replace with your color
        val spannableString = SpannableString(manageTextStr)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Your existing onClick logic
                val options = arrayOf(/*"Select More Photos",*/ "Go to Settings")
                val builder = AlertDialog.Builder(mContext)
                builder.setTitle("Manage Permissions")
                    .setItems(options) { dialog, which ->
                        when (which) {
                           /* 0 -> reRequestPermission()*/
                            0 -> {
                                Toast.makeText(mContext,"Allow always all or select more photos in limit access",Toast.LENGTH_SHORT).show()
                                // Action to go to settings
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri = Uri.fromParts("package", mContext?.packageName, null)
                                intent.data = uri
                                startActivity(intent)
                            }
                        }
                    }
                builder.create().show()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                ds.color = underlineColor
            }
        }

        val startIndex = manageTextStr.indexOf("Manage")
        val endIndex = startIndex + "Manage".length
        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(UnderlineSpan(), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(underlineColor), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        manageText.text = spannableString
        manageText.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun reRequestPermission() {
        // Launch the permission request dialog explicitly for multiple permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val permissionsToRequest = arrayOf(
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )

            if (permissionsToRequest.all {
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        it
                    ) == PackageManager.PERMISSION_GRANTED
                }) {
                // Permissions are already granted, but re-request them
                requestPermissionsLauncher.launch(permissionsToRequest)
            } else {
                // Some permissions are not granted, request them
                requestPermissionsLauncher.launch(permissionsToRequest)
            }
        }
    }

    private fun initView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerview)
        emptyView = view.findViewById(R.id.empty_view)
        manageText = view.findViewById(R.id.manageText)
        manageFrameLayout = view.findViewById(R.id.content)
        arguments?.let {
            fileType = it.getInt(BaseFragment.FILE_TYPE)
            imageFileSize = it.getInt(FilePickerConst.EXTRA_IMAGE_FILE_SIZE)
            videoFileSize = it.getInt(FilePickerConst.EXTRA_VIDEO_FILE_SIZE)
            activity?.let {
                imageCaptureManager = ImageCaptureManager(it)
            }
            val spanCount = PickerManager.spanTypes[FilePickerConst.SPAN_TYPE.DETAIL_SPAN] ?: 3
            val layoutManager = StaggeredGridLayoutManager(spanCount, OrientationHelper.VERTICAL)
            layoutManager.gapStrategy =
                StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
            recyclerView.layoutManager = layoutManager
            recyclerView.itemAnimator = DefaultItemAnimator()

            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    // Log.d(">>> Picker >>>", "dy = " + dy);
                    if (Math.abs(dy) > SCROLL_THRESHOLD) {
                        mGlideRequestManager.pauseRequests()
                    } else {
                        resumeRequestsIfNotDestroyed()
                    }
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        resumeRequestsIfNotDestroyed()
                    }
                }
            })
        }

        manageFrameLayout.visibility = if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.UPSIDE_DOWN_CAKE){
            View.GONE
        }
        else
        {
            View.GONE
        }
        viewModel.lvMediaData.observe(viewLifecycleOwner, Observer { data ->
            updateList(data)
        })

        viewModel.lvDataChanged.observe(viewLifecycleOwner, Observer {
            viewModel.getMedia(
                mediaType = fileType,
                imageFileSize = imageFileSize,
                videoFileSize = videoFileSize
            )
        })

        viewModel.getMedia(
            mediaType = fileType,
            imageFileSize = imageFileSize,
            videoFileSize = videoFileSize
        )
    }

    private fun updateList(medias: List<Media>) {
        view?.let { _ ->
            if (medias.isNotEmpty()) {
                emptyView.visibility = View.GONE
            } else {
                emptyView.visibility = View.VISIBLE
            }


            context?.let {
                if (photoGridAdapter != null) {
                    photoGridAdapter?.setData(medias, PickerManager.selectedPhotos)
                } else {
                    photoGridAdapter = PhotoGridAdapter(
                        it,
                        mGlideRequestManager,
                        medias,
                        PickerManager.selectedPhotos,
                        fileType == FilePickerConst.MEDIA_TYPE_IMAGE && PickerManager.isEnableCamera,
                        this
                    )
                    recyclerView.adapter = photoGridAdapter
                    photoGridAdapter?.setCameraListener(View.OnClickListener {
                        try {
                            uiScope.launch {
                                val intent =
                                    withContext(Dispatchers.IO) { imageCaptureManager?.dispatchTakePictureIntent() }
                                if (intent != null)
                                    startActivityForResult(
                                        intent,
                                        ImageCaptureManager.REQUEST_TAKE_PHOTO
                                    )
                                else
                                    Toast.makeText(
                                        activity,
                                        R.string.no_camera_exists,
                                        Toast.LENGTH_SHORT
                                    ).show()
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    })
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ImageCaptureManager.REQUEST_TAKE_PHOTO -> if (resultCode == Activity.RESULT_OK) {
                val imagePath = imageCaptureManager?.currentPhotoPath
                if (imagePath != null) {
                    if (PickerManager.getMaxCount() == 1) {
                        if (getFileSizeInMB(imagePath) <= 50) {
                            PickerManager.add(imagePath, FilePickerConst.FILE_TYPE_MEDIA)
                            Log.d(TAG, "onActivityResult: onItemSelected")
                            mListener?.onItemSelected()
                        } else {
                            Toast.makeText(
                                mContext,
                                String.format(
                                    mContext?.getString(R.string.maximum_file_size).toString(),
                                    FilePickerConst.MAX_FILE_SIZE
                                ),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } else {
                uiScope.launch(Dispatchers.IO) {
                    imageCaptureManager?.deleteContentUri(imageCaptureManager?.currentPhotoPath)
                }
            }
        }
    }

    private fun getFileSizeInMB(fileUri: Uri): Long {
        if (mContext != null) {
            val returnCursor: Cursor? =
                mContext!!.contentResolver.query(fileUri, null, null, null, null)
            /*
             * Get the column indexes of the data in the Cursor,
             * move to the first row in the Cursor, get the data,
             * and display it.
             */
            val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = returnCursor?.getColumnIndex(OpenableColumns.SIZE)
            returnCursor?.moveToFirst()
            if (returnCursor != null && sizeIndex != null) {
                val fileSizeInBytes: Long = returnCursor.getLong(sizeIndex)
                // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
                val fileSizeInKB = fileSizeInBytes / 1024
                // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
                val fileSizeInMB = fileSizeInKB / 1024
                Log.d(TAG, "getFileSizeInMB: " + fileSizeInMB)
                return fileSizeInMB
            } else {
                return 0
            }
        } else {
            return 0
        }
    }

    private fun resumeRequestsIfNotDestroyed() {
        if (!AndroidLifecycleUtils.canLoadImage(this)) {
            return
        }

        mGlideRequestManager.resumeRequests()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.select_menu, menu)
        selectAllItem = menu.findItem(R.id.action_select)
        menu.findItem(R.id.action_done)?.isVisible = PickerManager.getMaxCount() > 1
//        onItemSelected()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.action_select) {
            photoGridAdapter?.let { adapter ->
                adapter.selectAll()
                selectAllItem?.let {
                    if (it.isChecked) {
                        PickerManager.clearSelections()
                        adapter.clearSelection()
                        it.setIcon(R.drawable.ic_deselect_all)
                    } else {
                        adapter.selectAll()
                        PickerManager.add(adapter.selectedPaths, FilePickerConst.FILE_TYPE_MEDIA)
                        it.setIcon(R.drawable.ic_select_all)
                    }
                    selectAllItem?.isChecked = !it.isChecked
                    Log.d(TAG, "onOptionsItemSelected: onItemSelected")
                    mListener?.onItemSelected()
                }
            }
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    companion object {

        private val TAG = MediaDetailPickerFragment::class.java.simpleName
        private val SCROLL_THRESHOLD = 30

        fun newInstance(
            fileType: Int,
            imageFileSize: Int,
            videoFileSize: Int
        ): MediaDetailPickerFragment {
            val mediaDetailPickerFragment = MediaDetailPickerFragment()
            val bun = Bundle()
            bun.putInt(BaseFragment.Companion.FILE_TYPE, fileType)
            bun.putInt(FilePickerConst.EXTRA_IMAGE_FILE_SIZE, imageFileSize)
            bun.putInt(FilePickerConst.EXTRA_VIDEO_FILE_SIZE, videoFileSize)
            mediaDetailPickerFragment.arguments = bun
            return mediaDetailPickerFragment
        }
    }
}// Required empty public constructor
